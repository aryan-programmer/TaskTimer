package com.aryanstein.mcad.tasktimer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aryanstein.mcad.tasktimer.db.Tasks;
import com.aryanstein.mcad.tasktimer.db.Timings;

import java.security.InvalidParameterException;

public class FragmentMain
	extends Fragment
	implements LoaderManager.LoaderCallbacks<Cursor>,
	           TaskCursorRVAdapter.TaskClickListener {
	private static final String TAG = "FragmentMain";

	private static final int LOADER_ID = 119;

	private Timing currentTiming = null;

	private TaskCursorRVAdapter                   adapter;
	private TaskCursorRVAdapter.TaskClickListener adapterClickListener;

	// region ...Lifecycle
	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override public View onCreateView(
		LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState
	) {
		Activity     activity     = getActivity();
		View         view         = inflater.inflate(R.layout.fragment_main, container, false);
		RecyclerView recyclerView = view.findViewById(R.id.task_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(activity));
		if(activity instanceof TaskCursorRVAdapter.TaskClickListener) {
			adapterClickListener = (TaskCursorRVAdapter.TaskClickListener) activity;
		}
		if(adapter == null) {
			adapter = new TaskCursorRVAdapter(activity, null, this);
		} else {
			adapter.setListener(this);
		}
		recyclerView.setAdapter(adapter);
		return view;
	}

	@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_ID, null, this);
		setTimingText(currentTiming);
	}
	// endregion Lifecycle

	// region ...Helpers
	private void saveTiming(@NonNull Timing timing) {
		timing.updateDuration();

		ContentResolver contentResolver = getActivity().getContentResolver();
		ContentValues   values          = new ContentValues();
		values.put(Timings.TaskId, timing.getTask().getId());
		values.put(Timings.StartTime, timing.getStartTime());
		values.put(Timings.Duration, timing.getDuration());
		contentResolver.insert(Timings.CONTENT_URI, values);
	}

	private void setTimingText(Timing timing) {
		TextView currentTaskName = getActivity().findViewById(R.id.current_task_name);
		if(timing != null) {
			currentTaskName.setText(getString(R.string.timing_task_t_message,
			                                  currentTiming.getTask().getName()));
		} else {
			currentTaskName.setText(getString(R.string.no_task_message));
		}
	}
	// endregion Helpers

	// region ...Listener interface implementations
	@NonNull @Override public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
		Log.d(TAG, "onCreateLoader() called with: id = [" + id + "], args = [" + args + "]");
		if(id != LOADER_ID) {
			throw new InvalidParameterException(
				TAG + ".onCreateLoader called with invalid loader id " + id);
		}
		String[] proj = {
			Tasks._id,
			Tasks.Name,
			Tasks.Description,
			Tasks.SortOrder
		};
		String sortOrder =
			Tasks.SortOrder + "," +
			Tasks.Name + " COLLATE NOCASE";
		return new CursorLoader(
			requireActivity(),
			Tasks.CONTENT_URI,
			proj,
			null,
			null,
			sortOrder);
	}

	@SuppressLint("Range") @Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override public void onLoaderReset(@NonNull Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	@Override public void onTaskLongClick(@NonNull Task task) {
		if(adapterClickListener != null)
			adapterClickListener.onTaskLongClick(task);
		if(currentTiming != null) {
			if(task.getId() == currentTiming.getTask().getId()) {
				saveTiming(currentTiming);
				currentTiming = null;
			} else {
				saveTiming(currentTiming);
				currentTiming = new Timing(task);
			}
		} else {
			currentTiming = new Timing(task);
		}
		setTimingText(currentTiming);
	}

	@Override public void onTaskEditClick(@NonNull Task task) {
		if(adapterClickListener != null)
			adapterClickListener.onTaskEditClick(task);
	}

	@Override public void onTaskDeleteClick(@NonNull Task task) {
		if(adapterClickListener != null)
			adapterClickListener.onTaskDeleteClick(task);
	}
	// endregion Listener interface implementations
}