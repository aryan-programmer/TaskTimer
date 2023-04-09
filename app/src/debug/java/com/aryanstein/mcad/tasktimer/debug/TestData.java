package com.aryanstein.mcad.tasktimer.debug;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.aryanstein.mcad.tasktimer.db.Tasks;
import com.aryanstein.mcad.tasktimer.db.Timings;

import java.util.GregorianCalendar;

public class TestData {
	private static final String TAG = "TestData";

	public static void generateTestData(ContentResolver contentResolver) {

		final int SECONDS_IN_DAY = 60 * 60 * 24;
		final int LOWER_BOUND    = 100;
		final int UPPER_BOUND    = 500;
		final int MAX_DURATION   = SECONDS_IN_DAY / 6;
		String[] projection = {
			Tasks._id
		};
		Uri uri = Tasks.CONTENT_URI;
		try(Cursor cursor = contentResolver.query(uri, projection, null, null, null)) {
			if(cursor != null && cursor.moveToFirst()) {
				int idCol = cursor.getColumnIndex(Tasks._id);
				do {
					long taskId    = cursor.getLong(idCol);
					int  loopCount = LOWER_BOUND + getRandomInt(UPPER_BOUND - LOWER_BOUND + 1);
					for(int i = 0; i < loopCount; i++) {
						long randomDate = randomDateTime();
						long duration   = (long) getRandomInt(MAX_DURATION);

						com.aryanstein.mcad.tasktimer.debug.TestTiming timing = new com.aryanstein.mcad.tasktimer.debug.TestTiming(
							taskId, randomDate, duration);
						saveCurrentTiming(contentResolver, timing);
					}
				} while(cursor.moveToNext());
			}
		}

	}

	private static int getRandomInt(int max) {
		return (int) (Math.round(Math.random() * (max)));
	}

	private static long randomDateTime() {
		final int startYear = 2023;
		final int endYear   = 2024;

		int sec   = getRandomInt(59);
		int min   = getRandomInt(59);
		int hour  = getRandomInt(23);
		int month = getRandomInt(11);
		int year  = startYear + getRandomInt(endYear - startYear);

		GregorianCalendar gc = new GregorianCalendar(year, month, 1);
		int day =
			1 + getRandomInt(gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH) - 1);
		gc.set(year, month, day, hour, min, sec);
		return gc.getTimeInMillis();
	}

	private static void saveCurrentTiming(
		ContentResolver contentResolver,
		com.aryanstein.mcad.tasktimer.debug.TestTiming timing
	) {
		ContentValues values = new ContentValues();
		values.put(Timings.TaskId, timing.taskId);
		values.put(Timings.StartTime, timing.startTime);
		values.put(Timings.Duration, timing.duration);
		contentResolver.insert(Timings.CONTENT_URI, values);
	}
}
