package com.aryanstein.mcad.tasktimer;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aryanstein.mcad.tasktimer.db.Durations;

import java.util.Locale;
import java.util.Objects;

class DurationsCursorRVAdapter
	extends RecyclerView.Adapter<DurationsCursorRVAdapter.DurationsViewHolder> {
	private static final String TAG = "DurationsCursorRVAdapte";

	private       Context              context;
	private       Cursor               cursor;
	private final java.text.DateFormat dateFormat;

	public DurationsCursorRVAdapter(Context context, Cursor cursor) {
		this.context = context;
		this.cursor  = cursor;
		dateFormat   = DateFormat.getMediumDateFormat(context);
	}

	@NonNull @Override
	public DurationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
		                          .inflate(R.layout.task_duration_list_item, parent, false);
		return new DurationsViewHolder(view);
	}

	@Override public void onBindViewHolder(@NonNull DurationsViewHolder holder, int i) {
		if(!isCursorEmpty()) {
			if(!cursor.moveToPosition(i)) {
				throw new IllegalStateException("Couldn't move cursor to position " + i);
			}
			int    col_id         = cursor.getColumnIndex(Durations._id);
			int    colName        = cursor.getColumnIndex(Durations.Name);
			int    colDescription = cursor.getColumnIndex(Durations.Description);
			int    colStartTime   = cursor.getColumnIndex(Durations.StartTime);
			int    colStartDate   = cursor.getColumnIndex(Durations.StartDate);
			int    colDuration    = cursor.getColumnIndex(Durations.Duration);
			String name           = cursor.getString(colName);
			String description    = cursor.getString(colDescription);
			long   startTime      = cursor.getLong(colStartTime);
			long   totalDuration  = cursor.getLong(colDuration);
			// DB stores seconds, DateFormat need milliseconds
			String userDate  = dateFormat.format(startTime * 1000);
			String totalTime = formatDuration(totalDuration);
			if(BuildConfig.DEBUG) {
				//userDate = cursor.getString(colStartDate);
			}

			holder.name.setText(name);
			if(holder.description != null) {
				holder.description.setText(description);
			}
			holder.startDate.setText(userDate);
			holder.duration.setText(totalTime);
		}
	}

	@Override public int getItemCount() {
		return cursor == null ? 0 : cursor.getCount();
	}

	private boolean isCursorEmpty() {
		return cursor == null || cursor.getCount() == 0;
	}

	/**
	 * @param duration Duration in seconds
	 * @return Formatted duration hh:mm:ss
	 */
	private String formatDuration(long duration) {
		// Allows hrs>24, so can't use TimeDate
		long hrs = duration / 3600;
		long rem = duration - (hrs * 3600);
		long min = rem / 60;
		long sec = rem - (min * 60);
		return String.format(Locale.US, "%02d:%02d:%02d", hrs, min, sec);
	}

	/**
	 * Swaps in a new Cursor, returning the old Cursor.
	 * The returned old Cursor is <em>not</em> closed;
	 *
	 * @param cursor The new cursor
	 * @return The previous cursor, null if there wasn't.
	 * If the given cursor id the same instance as the old Cursor, null is returned
	 * @see Cursor
	 */
	Cursor swapCursor(Cursor cursor) {
		if(Objects.equals(cursor, this.cursor)) {
			return null;
		}
		int    oldCount = getItemCount();
		Cursor t        = this.cursor;
		this.cursor = cursor;
		if(cursor != null) {
			//Log.d(TAG, "swapCursor: " + cursor.getCount());
			notifyDataSetChanged();
		} else {
			notifyItemRangeRemoved(0, oldCount);
		}
		return t;
	}

	static class DurationsViewHolder extends RecyclerView.ViewHolder {
		// Button extends TextView
//		Button nameB, descriptionB, startDateB, durationB;
		View     view;
		TextView name, description, startDate, duration;

		public DurationsViewHolder(@NonNull View view) {
			super(view);
			this.view   = view;
			name        = view.findViewById(R.id.dur_item__name_view);
			description = view.findViewById(R.id.dur_item__description_view);
			startDate   = view.findViewById(R.id.dur_item__start_date_view);
			duration    = view.findViewById(R.id.dur_item__duration_view);
		}
	}
}
