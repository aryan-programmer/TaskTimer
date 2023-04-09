package com.aryanstein.mcad.tasktimer;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aryanstein.mcad.tasktimer.db.Durations;
import com.aryanstein.mcad.tasktimer.db.Timings;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DurationsReportActivity
	extends AppCompatActivity
	implements LoaderManager.LoaderCallbacks<Cursor>,
	           DatePickerDialog.OnDateSetListener,
	           AppDialog.DialogListener,
	           View.OnClickListener {
	private static final String TAG = "DurationsReportActivity";

	public static void start(Context context) {
		Intent starter = new Intent(context, DurationsReportActivity.class);
		context.startActivity(starter);
	}

	private static final int    LOADER_ID              = 22404;
	public static final  int    DIALOG_DATE_FILTER     = 40611;
	public static final  int    DIALOG_DATE_DELETE     = 34195;
	public static final  int    DIALOG_DELETE_CONFIRM  = 52350;
	private static final String KEY_SELECTION          = "SELECTION";
	private static final String KEY_SELECTION_ARGS     = "SELECTION_ARGS";
	private static final String KEY_SORT_ORDER         = "SORT_ORDER";
	private static final String KEY_DELETE_DATE_MILLIS = "DELETE_DATE";
	public static final  String KEY_CURRENT_DATE       = "CURRENT_DATE";
	public static final  String KEY_DISPLAY_WEEK       = "DISPLAY_WEEK";

	private final GregorianCalendar calendar = new GregorianCalendar();

	private Bundle  args        = new Bundle();
	private boolean displayWeek = true;

	private DurationsCursorRVAdapter adapter;

	// region ...Lifecycle
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_durations_report);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		if(savedInstanceState != null) {
			long dateMillis = savedInstanceState.getLong(KEY_CURRENT_DATE, 0);
			if(dateMillis != 0) {
				calendar.setTimeInMillis(dateMillis);
				calendar.clear(Calendar.HOUR_OF_DAY);
				calendar.clear(Calendar.MINUTE);
				calendar.clear(Calendar.SECOND);
			}
			displayWeek = savedInstanceState.getBoolean(KEY_DISPLAY_WEEK, true);
		}
		applyFilter();

		findViewById(R.id.dur__name_h1btn).setOnClickListener(this);
		View descriptionsHeader = findViewById(R.id.dur__description_h1btn);
		if(descriptionsHeader != null)
			descriptionsHeader.setOnClickListener(this);
		findViewById(R.id.dur__duration_h1btn).setOnClickListener(this);
		findViewById(R.id.dur__start_date_h1btn).setOnClickListener(this);

		RecyclerView recyclerView = findViewById(R.id.dur__durations_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		if(adapter == null) {
			adapter = new DurationsCursorRVAdapter(getApplicationContext(), null);
		}
		recyclerView.setAdapter(adapter);

		getSupportLoaderManager().initLoader(LOADER_ID, args, this);
	}

	@Override protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(KEY_CURRENT_DATE, calendar.getTimeInMillis());
		outState.putBoolean(KEY_DISPLAY_WEEK, displayWeek);
	}

	// endregion Lifecycle

	// region ...Menu
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_durations_report, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int itemId = item.getItemId();
		if(itemId == R.id.dur_menu__period) {
			displayWeek = !displayWeek;
			applyFilter();
			restartLoader();
			invalidateOptionsMenu();
			return true;
		} else if(itemId == R.id.dur_menu__date) {
			showDatePickerDialog(getString(R.string.dur_date_filter_diag__message),
			                     DIALOG_DATE_FILTER);
			return true;
		} else if(itemId == R.id.dur_menu__delete) {
			showDatePickerDialog(getString(R.string.dur_date_del_upto_diag__message),
			                     DIALOG_DATE_DELETE);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem period = menu.findItem(R.id.dur_menu__period);
		if(period != null) {
			if(displayWeek) {
				// If displaying week filter offer switch to day filter
				period.setIcon(R.drawable.ic_baseline_filter_1_24);
				period.setTitle(R.string.dur_menu__period__day);
			} else {
				period.setIcon(R.drawable.ic_baseline_filter_7_24);
				period.setTitle(R.string.dur_menu__period__week);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	// endregion Menu

	// region ...Helpers
	private void showDatePickerDialog(String title, int diagId) {
		DialogFragment dialogFragment = new DatePickerFragment();
		Bundle         args           = new Bundle();
		args.putInt(DatePickerFragment.KEY_ID, diagId);
		args.putString(DatePickerFragment.KEY_TITLE, title);
		args.putSerializable(DatePickerFragment.KEY_DATE, calendar.getTime());

		dialogFragment.setArguments(args);
		dialogFragment.show(getSupportFragmentManager(), DatePickerFragment.TAG);
	}

	private void applyFilter() {
		if(displayWeek) {
			Date currentCalDate = calendar.getTime();
			int  dayOfWeek      = calendar.get(Calendar.DAY_OF_WEEK);
			int  weekStart      = calendar.getFirstDayOfWeek();
			// Go to start of week
			calendar.set(Calendar.DAY_OF_WEEK, weekStart);
			String startDate = String.format(
				Locale.US,
				"%04d-%02d-%02d",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH));
			// + 6 days for last day of week
			calendar.add(Calendar.DATE, 6);
			String endDate = String.format(
				Locale.US,
				"%04d-%02d-%02d",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH));
			String[] selectionArgs = new String[] {
				startDate, endDate
			};

			args.putString(KEY_SELECTION, "StartDate BETWEEN ? AND ?");
			args.putStringArray(KEY_SELECTION_ARGS, selectionArgs);

			// Restore calender field
			calendar.setTime(currentCalDate);
		} else {
			String startDate = String.format(
				Locale.US,
				"%04d-%02d-%02d",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH));
			String[] selectionArgs = new String[] {
				startDate
			};
			args.putString(KEY_SELECTION, "StartDate = ?");
			args.putStringArray(KEY_SELECTION_ARGS, selectionArgs);
		}
	}

	private void restartLoader() {
		getSupportLoaderManager().restartLoader(LOADER_ID, args, this);
	}

	private void deleteRecords(long dateInMillis) {
		long            dateInSeconds = dateInMillis / 1000;
		String[]        selectionArgs = new String[] {String.valueOf(dateInSeconds)};
		String          selection     = Timings.StartTime + " < ?";
		ContentResolver resolver      = getContentResolver();
		resolver.delete(Timings.CONTENT_URI, selection, selectionArgs);
		applyFilter();
		restartLoader();
	}
	// endregion Helpers

	// region ...Interface implementations
	// region ...LoaderManager.LoaderCallbacks<Cursor>
	@NonNull @Override public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
		if(id != LOADER_ID) {
			throw new InvalidParameterException(
				TAG + ".onCreateLoader called with invalid loader id " + id);
		}
		String[] proj = {
			Durations._id,
			Durations.Name,
			Durations.Description,
			Durations.StartTime,
			Durations.StartDate,
			Durations.Duration
		};
		String   selection     = null;
		String[] selectionArgs = null;
		String   sortOrder     = null;
		if(args != null) {
			selection     = args.getString(KEY_SELECTION);
			selectionArgs = args.getStringArray(KEY_SELECTION_ARGS);
			sortOrder     = args.getString(KEY_SORT_ORDER);
		}
		return new CursorLoader(
			this,
			Durations.CONTENT_URI,
			proj,
			selection,
			selectionArgs,
			sortOrder);
	}

	@SuppressLint("Range") @Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
		Log.d(TAG, "onLoadFinished(): count: " + data.getCount());
	}

	@Override public void onLoaderReset(@NonNull Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
	// endregion LoaderManager.LoaderCallbacks<Cursor>

	// region ...DatePickerDialog.OnDateSetListener
	@Override public void onDateSet(DatePicker view, int y, int m, int d) {
		calendar.set(y, m, d, 0, 0, 0);
		int diagId = (int) view.getTag();
		switch(diagId) {
		case DIALOG_DATE_FILTER:
			applyFilter();
			restartLoader();
			break;
		case DIALOG_DATE_DELETE:
			long timeInMillis = calendar.getTimeInMillis();
			String dateFormatted = DateFormat.getDateFormat(this)
			                                 .format(timeInMillis);
			AppDialog dialog = new AppDialog();
			Bundle args = new Bundle();
			args.putInt(AppDialog.KEY_ID, DIALOG_DELETE_CONFIRM);
			args.putString(AppDialog.KEY_MESSAGE,
			               getString(R.string.delete_timings_diag__message, dateFormatted));
			args.putLong(KEY_DELETE_DATE_MILLIS, timeInMillis);
			dialog.setArguments(args);
			dialog.show(getSupportFragmentManager(), null);
			break;
		default:
			throw new IllegalArgumentException("Illegal dialog ID");
		}
	}
	// endregion DatePickerDialog.OnDateSetListener

	// region ...AppDialog.DialogListener
	@Override public void onDialogResult(AppDialog.Result result, int dialogId, Bundle args) {
		if(result == AppDialog.Result.Positive) {
			deleteRecords(args.getLong(KEY_DELETE_DATE_MILLIS));
		}
	}
	// endregion AppDialog.DialogListener

	// region ...View.OnClickListener
	@Override public void onClick(View view) {
		int id = view.getId();
		if(id == R.id.dur__name_h1btn) {
			args.putString(KEY_SORT_ORDER, Durations.Name);
		} else if(id == R.id.dur__description_h1btn) {
			args.putString(KEY_SORT_ORDER, Durations.Description);
		} else if(id == R.id.dur__start_date_h1btn) {
			args.putString(KEY_SORT_ORDER, Durations.StartDate);
		} else if(id == R.id.dur__duration_h1btn) {
			args.putString(KEY_SORT_ORDER, Durations.Duration);
		}
		restartLoader();
	}
	// endregion View.OnClickListener
	// endregion Interface implementations
}