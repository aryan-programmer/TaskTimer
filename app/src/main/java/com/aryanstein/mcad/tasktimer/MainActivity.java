package com.aryanstein.mcad.tasktimer;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aryanstein.mcad.tasktimer.db.Tasks;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity
	extends AppCompatActivity
	implements FragmentAddEdit.SaveClickListener,
	           AppDialog.DialogListener,
	           TaskCursorRVAdapter.TaskClickListener {
	private static final String TAG = "MainActivity";

	public static final int DIALOG_ID__DELETE      = 23897;
	public static final int DIALOG_ID__CANCEL_EDIT = 37200;

	private boolean twoPane = false;

	private AlertDialog aboutDialog;
	private AlertDialog instructionsDialog;
	private View        mainFrag;
	private View        addEditFrag;

	// region ...Lifecycle
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mainFrag    = findViewById(R.id.main__fragment_main);
		addEditFrag = findViewById(R.id.main__frag_add_edit);

		twoPane =
			getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

		FragmentManager fragmentManager = getSupportFragmentManager();
		boolean editing =
			fragmentManager.findFragmentById(R.id.main__frag_add_edit) != null;

		if(twoPane) {
			mainFrag.setVisibility(View.VISIBLE);
			addEditFrag.setVisibility(View.VISIBLE);
		} else {
			if(editing) {
				mainFrag.setVisibility(View.GONE);
				addEditFrag.setVisibility(View.VISIBLE);
			} else {
				mainFrag.setVisibility(View.VISIBLE);
				addEditFrag.setVisibility(View.GONE);
			}
		}
	}

	@Override protected void onStop() {
		super.onStop();
		dismissAboutDialog();
		dismissInstructionsDialog();
	}
	// endregion Lifecycle

	@Override public void onBackPressed() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentAddEdit frag            = (FragmentAddEdit) fragmentManager.findFragmentById(R.id.main__frag_add_edit);
		if((frag == null) || frag.canClose()) {
			super.onBackPressed();
		} else {
			showBackConfirmationDialog();
		}
	}

	// region ...Menu
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		if(BuildConfig.DEBUG) {
			MenuItem generate = menu.findItem(R.id.menu_main__generate);
			generate.setVisible(true);
		}
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if(id == R.id.menu_main__add_task) {
			taskEditReq(null);
			return true;
		} else if(id == R.id.menu_main__show_durations) {
			DurationsReportActivity.start(this);
			return true;
		} else if(id == R.id.menu_main__instructions) {
			showInstructionsDialog();
			return true;
		} else if(id == R.id.menu_main__show_about) {
			showAboutDialog();
			return true;
		} else if(id == R.id.menu_main__generate) {
			if(BuildConfig.DEBUG) {
				com.aryanstein.mcad.tasktimer.debug.TestData.generateTestData(getContentResolver());
			}
			return true;
		} else if(id == android.R.id.home) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentAddEdit frag            = (FragmentAddEdit) fragmentManager.findFragmentById(R.id.main__frag_add_edit);
			if((frag == null) || frag.canClose()) {
				return super.onOptionsItemSelected(item);
			} else {
				showBackConfirmationDialog();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
	// endregion Menu

	// region ...Dialogs
	private void showBackConfirmationDialog() {
		AppDialog dialog = new AppDialog();
		Bundle    args   = new Bundle();
		args.putInt(AppDialog.KEY_ID, DIALOG_ID__CANCEL_EDIT);
		args.putString(
			AppDialog.KEY_MESSAGE,
			getString(R.string.cancel_edit_diag__message)
		);
		args.putInt(AppDialog.KEY_POSITIVE_TEXT_ID, R.string.cancel_edit_diag__pos_caption);
		args.putInt(AppDialog.KEY_NEGATIVE_TEXT_ID, R.string.cancel_edit_diag__neg_caption);
		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), null);
	}

	private void showInstructionsDialog() {
		View                       messageView = getLayoutInflater().inflate(R.layout.instructions_dialog, null, false);
		MaterialAlertDialogBuilder builder     = new MaterialAlertDialogBuilder(this);
		builder
			.setView(messageView)
			.setTitle(R.string.app_name)
			.setIcon(R.mipmap.ic_launcher)
			.setPositiveButton(R.string.ok, (dialogInterface, i) -> dismissInstructionsDialog());
		instructionsDialog = builder.create();
		instructionsDialog.setCanceledOnTouchOutside(true);
		instructionsDialog.show();
	}

	private void dismissInstructionsDialog() {
		if(instructionsDialog != null && instructionsDialog.isShowing()) {
			instructionsDialog.dismiss();
		}
	}

	@SuppressLint("SetTextI18n") private void showAboutDialog() {
		View     messageView  = getLayoutInflater().inflate(R.layout.about_dialog, null, false);
		TextView versionTextV = messageView.findViewById(R.id.about__version);
		versionTextV.setText("v" + BuildConfig.VERSION_NAME);
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder
			.setView(messageView)
			.setTitle(R.string.app_name)
			.setIcon(R.mipmap.ic_launcher)
			.setPositiveButton(R.string.ok, (dialogInterface, i) -> dismissAboutDialog());
		aboutDialog = builder.create();
		aboutDialog.setCanceledOnTouchOutside(true);
		aboutDialog.show();
	}

	private void dismissAboutDialog() {
		if(aboutDialog != null && aboutDialog.isShowing()) {
			aboutDialog.dismiss();
		}
	}
	// endregion Dialogs

	// region ...Helpers
	private void taskEditReq(Task task) {
		FragmentAddEdit frag     = new FragmentAddEdit();
		Bundle          fragArgs = new Bundle();
		fragArgs.putSerializable(Task.TAG, task);
		frag.setArguments(fragArgs);

		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.main__frag_add_edit, frag)
			.commit();
		if(!twoPane) {
			mainFrag.setVisibility(View.GONE);
			addEditFrag.setVisibility(View.VISIBLE);
		}
	}

	private void closeAddEditFrag() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment        frag            = fragmentManager.findFragmentById(R.id.main__frag_add_edit);
		if(frag != null) {
			fragmentManager
				.beginTransaction()
				.remove(frag)
				.commit();
		}

		if(!twoPane) {
			addEditFrag.setVisibility(View.GONE);
			mainFrag.setVisibility(View.VISIBLE);
		}
	}
	// endregion Helpers

	// region ...Listener interface implementations
	@Override public void onTaskLongClick(@NonNull Task task) {
		// Blank
	}

	@Override public void onTaskEditClick(@NonNull Task task) {
		taskEditReq(task);
	}

	@Override public void onTaskDeleteClick(@NonNull Task task) {
		AppDialog dialog = new AppDialog();
		Bundle    args   = new Bundle();
		args.putInt(AppDialog.KEY_ID, DIALOG_ID__DELETE);
		args.putString(
			AppDialog.KEY_MESSAGE,
			getString(R.string.del_diag__message, task.getId(), task.getName())
		);
		args.putInt(AppDialog.KEY_POSITIVE_TEXT_ID, R.string.del_diag__pos_caption);
		args.putSerializable(Task.TAG, task);
		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), null);
	}

	@Override public void onFragAddEditSaveClicked() {
		closeAddEditFrag();
	}

	@Override public void onDialogResult(AppDialog.Result result, int dialogId, Bundle args) {
		switch(dialogId) {
		case DIALOG_ID__DELETE:
			if(result == AppDialog.Result.Positive) {
				Task task = (Task) args.getSerializable(Task.class.getSimpleName());
				getContentResolver().delete(
					Tasks.buildUri(task.getId()),
					null,
					null
				);
			}
			break;
		case DIALOG_ID__CANCEL_EDIT:
			if(result == AppDialog.Result.Positive) {
				closeAddEditFrag();
			}
			break;
		}
	}
	// endregion Listener interface implementations
}