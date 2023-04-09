package com.aryanstein.mcad.tasktimer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
	public static final String TAG       = "DatePickerFragment";
	public static final  String KEY_ID    = "ID";
	public static final  String KEY_TITLE = "TITLE";
	public static final  String KEY_DATE  = "DATE";
	int diagId;

	@NonNull @Override public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		GregorianCalendar cal   = new GregorianCalendar();
		String            title = null;
		Bundle            args  = getArguments();
		if(args != null) {
			diagId = args.getInt(KEY_ID);
			title  = args.getString(KEY_TITLE);
			Date d = (Date) args.getSerializable(KEY_DATE);
			if(d != null) {
				cal.setTime(d);
				Log.d(TAG, "onCreateDialog: " + d.toString());
			}
		}
		int              y   = cal.get(Calendar.YEAR);
		int              m   = cal.get(Calendar.MONTH);
		int              d   = cal.get(Calendar.DAY_OF_MONTH);
		DatePickerDialog dpd = new DatePickerDialog(getContext(), this, y, m, d);
		if(title != null) {
			dpd.setTitle(title);
		}
		return dpd;
	}

	@Override public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if(!(context instanceof DatePickerDialog.OnDateSetListener)) {
			throw new ClassCastException(
				context.toString() + " must implement DatePickerDialog.OnDateSetListener");
		}
	}

	@Override public void onDateSet(DatePicker view, int y, int m, int d) {
		DatePickerDialog.OnDateSetListener l = (DatePickerDialog.OnDateSetListener) getActivity();
		if(l != null) {
			view.setTag(diagId);
			l.onDateSet(view, y, m, d);
		}
		Log.d(TAG, "onDateSet: Exit");
	}
}
