package com.aryanstein.mcad.tasktimer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aryanstein.mcad.tasktimer.db.Tasks;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class FragmentAddEdit extends Fragment {
	private static final String TAG = "FragmentAddEdit";

	public enum FragmentEditMode {Edit, Add}

	private FragmentEditMode editMode;

	private EditText name, description, sortOrder;
	private Button save;

	private SaveClickListener saveClickListener;

	@Override public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		Activity activity = getActivity();
		if(activity instanceof SaveClickListener) {
			saveClickListener = (SaveClickListener) activity;
		}
	}

	@Override
	public View onCreateView(
		LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState
	) {
		View v = inflater.inflate(R.layout.fragment_add_edit, container, false);

		name        = v.findViewById(R.id.add_edit__name);
		description = v.findViewById(R.id.add_edit__description);
		sortOrder   = v.findViewById(R.id.add_edit__sort_order);
		save        = v.findViewById(R.id.add_edit__save_btn);

		// TODO: Change
//		Bundle args = getActivity().getIntent().getExtras();
		Bundle args = getArguments();

		final Task task;
		if(args != null) {
			task = (Task) args.getSerializable(Task.class.getSimpleName());
			if(task != null) {
				name.setText(task.getName());
				description.setText(task.getDescription());
				sortOrder.setText(Integer.toString(task.getSortOrder()));
				editMode = FragmentEditMode.Edit;
			} else {
				editMode = FragmentEditMode.Add;
			}
		} else {
			task     = null;
			editMode = FragmentEditMode.Add;
		}

		Log.d(TAG, "onCreateView(): editMode: " + editMode);

		save.setOnClickListener(view -> {
			int sortOrd = 0;
			if(sortOrder.length() > 0) {
				sortOrd = Integer.parseInt(sortOrder.getText().toString());
			}

			String error = null;

			String nName        = name.getText().toString();
			String nDescription = description.getText().toString();

			ContentResolver contentResolver = getActivity().getContentResolver();
			ContentValues   values          = new ContentValues();
			if(editMode == FragmentEditMode.Edit && task != null) {
				if(!Objects.equals(nName, task.getName()) && nName.length() > 0) {
					values.put(Tasks.Name, nName);
				}
				if(!Objects.equals(nDescription, task.getDescription()) &&
				   description.length() > 0) {
					values.put(Tasks.Description, nDescription);
				}
				if(sortOrd != task.getSortOrder()) {
					values.put(Tasks.SortOrder, sortOrd);
				}
				if(values.size() != 0) {
					contentResolver.update(
						Tasks.buildUri(task.getId()),
						values,
						null,
						null);
				} else {
					error = "No values have been changed";
				}
			} else if(editMode == FragmentEditMode.Add) {
				if(nName.length() > 0) {
					values.put(Tasks.Name, nName);
					values.put(Tasks.Description, nDescription);
					values.put(Tasks.SortOrder, sortOrd);
					contentResolver.insert(Tasks.CONTENT_URI, values);
				} else {
					error = "Enter a name";
				}
			}
			if(error == null) {
				if(saveClickListener != null) {
					saveClickListener.onFragAddEditSaveClicked();
				}
//				getActivity().finish();
			} else {
				Snackbar.make(save, error, BaseTransientBottomBar.LENGTH_LONG).show();
			}
		});

		return v;
	}

	@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if(actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override public void onDetach() {
		super.onDetach();
		saveClickListener = null;
		ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if(actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(false);
		}
	}

	public boolean canClose() {
		return false;
	}

	public interface SaveClickListener {
		void onFragAddEditSaveClicked();
	}
}