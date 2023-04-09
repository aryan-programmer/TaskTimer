package com.aryanstein.mcad.tasktimer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AppDialog extends DialogFragment {
	private static final String TAG = "AppDialog";

	public static final String KEY_ID               = "ID";
	public static final String KEY_TITLE            = "TITLE";
	public static final String KEY_MESSAGE          = "MESSAGE";
	public static final String KEY_POSITIVE_TEXT_ID = "POSITIVE_TEXT_ID";
	public static final String KEY_NEGATIVE_TEXT_ID = "NEGATIVE_TEXT_ID";

	private DialogListener dialogListener;

	@Override public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		Activity activity = getActivity();
		if(activity instanceof DialogListener) {
			dialogListener = (DialogListener) activity;
		}
	}

	@NonNull @Override public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
		final Bundle               args    = getArguments();
		final int                  dialogId;
		String                     title;
		String                     message;
		int                        positiveId;
		int                        negativeId;

		if(args != null) {
			dialogId = args.getInt(KEY_ID);
			message  = args.getString(KEY_MESSAGE);
			if(dialogId == 0 || message == null) {
				throw new IllegalArgumentException(
					"Must pass DIALOG_ID and DIALOG_MESSAGE in the bundle");
			}
			title      = args.getString(KEY_TITLE, "");
			positiveId = args.getInt(KEY_POSITIVE_TEXT_ID);
			negativeId = args.getInt(KEY_NEGATIVE_TEXT_ID);
			if(positiveId == 0) positiveId = R.string.ok;
			if(negativeId == 0) negativeId = R.string.cancel;
			if(title.length() == 0) title = getString(R.string.app_name);
		} else {
			throw new IllegalArgumentException(
				"Must pass DIALOG_ID and DIALOG_MESSAGE in the bundle");
		}

		builder
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(positiveId, (dialogInterface, i) -> {
				if(dialogListener != null) {
					dialogListener.onDialogResult(Result.Positive, dialogId, args);
				}
			})
			.setNegativeButton(negativeId, (dialogInterface, i) -> {
				if(dialogListener != null) {
					dialogListener.onDialogResult(Result.Negative, dialogId, args);
				}
			});

		return builder.create();
	}

	@Override public void onDetach() {
		super.onDetach();
		dialogListener = null;
	}

	@Override public void onCancel(@NonNull DialogInterface dialog) {
		if(dialogListener != null) {
			dialogListener.onCancelled(getArguments().getInt(KEY_ID));
		}
	}

	public enum Result {Positive, Negative}

	public interface DialogListener {
		void onDialogResult(Result result, int dialogId, Bundle args);

		default void onCancelled(int dialogId) {
		}
	}
}
