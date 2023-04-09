package com.aryanstein.mcad.tasktimer.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppProvider extends ContentProvider {
	private static final String TAG = "AppProvider";

	private AppDb openHelper;

	private static final UriMatcher uriMatcher = buildUriMatcher();

	static final        String CONTENT_AUTHORITY     = "com.aryanstein.mcad.tasktimer.provider";
	public static final Uri    CONTENT_AUTHORITY_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	private static final int TASKS             = 100;
	private static final int TASKS_ID          = 101;
	private static final int TIMINGS           = 200;
	private static final int TIMINGS_ID        = 201;
	//	private static final int TASK_TIMINGS      = 300;
//	private static final int TASK_TIMINGS_ID   = 301;
	private static final int TASK_DURATIONS    = 400;
	private static final int TASK_DURATIONS_ID = 401;

	private static UriMatcher buildUriMatcher() {
		final UriMatcher m = new UriMatcher(UriMatcher.NO_MATCH);
		// eg. content://com.aryanstein.mcad.tasktimer.provider/Tasks
		m.addURI(CONTENT_AUTHORITY, Tasks.Table, TASKS);
		// eg. content://com.aryanstein.mcad.tasktimer.provider/Tasks/8
		m.addURI(CONTENT_AUTHORITY, Tasks.Table + "/#", TASKS_ID);

		m.addURI(CONTENT_AUTHORITY, Timings.Table, TIMINGS);
		m.addURI(CONTENT_AUTHORITY, Timings.Table + "/#", TIMINGS_ID);

		m.addURI(CONTENT_AUTHORITY, Durations.Table, TASK_DURATIONS);
		m.addURI(CONTENT_AUTHORITY, Durations.Table + "/#", TASK_DURATIONS_ID);

		return m;
	}

	@Override public boolean onCreate() {
		openHelper = AppDb.getInstance(getContext());
		return true;
	}

	@Nullable @Override
	public Cursor query(@NonNull Uri uri,
	                    @Nullable String[] projection,
	                    @Nullable String selection,
	                    @Nullable String[] selectionArgs,
	                    @Nullable String sortOrder) {
		int match = uriMatcher.match(uri);
		Log.d(TAG, "query: match: " + match);
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch(match) {
		case TASKS:
			qb.setTables(Tasks.Table);
			break;
		case TASKS_ID:
			qb.setTables(Tasks.Table);
			long taskId = Tasks.getId(uri);
			qb.appendWhere(Tasks._id + " = " + taskId);
			break;

		case TIMINGS:
			qb.setTables(Timings.Table);
			break;
		case TIMINGS_ID:
			qb.setTables(Timings.Table);
			long timingsId = Timings.getId(uri);
			qb.appendWhere(Timings._id + " = " + timingsId);
			break;

		case TASK_DURATIONS:
			qb.setTables(Durations.Table);
			break;
		case TASK_DURATIONS_ID:
			qb.setTables(Durations.Table);
			long durationsId = Durations.getId(uri);
			qb.appendWhere(Durations._id + " = "+durationsId);
			break;

		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}

		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = qb.query(db,
		                         projection,
		                         selection,
		                         selectionArgs,
		                         null,
		                         null,
		                         sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Nullable @Override public String getType(@NonNull Uri uri) {
		int match = uriMatcher.match(uri);
		switch(match) {
		case TASKS:
			return Tasks.CONTENT_TYPE;
		case TASKS_ID:
			return Tasks.CONTENT_ITEM_TYPE;
		case TIMINGS:
			return Timings.CONTENT_TYPE;
		case TIMINGS_ID:
			return Timings.CONTENT_ITEM_TYPE;
		case TASK_DURATIONS:
			return Durations.CONTENT_TYPE;
		case TASK_DURATIONS_ID:
			return Durations.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	@Nullable @Override public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
		int                  match = uriMatcher.match(uri);
		final SQLiteDatabase db;
		Uri                  retUri;
		long                 recordId;
		Log.d(TAG, "insert() called with: uri = [" + uri + "], values = [" + values + "]");
		switch(match) {
		case TASKS:
			db = openHelper.getWritableDatabase();
			recordId = db.insert(Tasks.Table, null, values);
			if(recordId >= 0) {
				retUri = Tasks.buildUri(recordId);
			} else {
				throw new SQLException("Failed to insert into " + uri.toString());
			}
			break;
		case TIMINGS:
			db = openHelper.getWritableDatabase();
			recordId = db.insert(Timings.Table, null, values);
			if(recordId >= 0) {
				retUri = Timings.buildUri(recordId);
			} else {
				throw new SQLException("Failed to insert into " + uri.toString());
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		Log.d(TAG, "insert() returned: " + retUri);
		return retUri;
	}

	@Override
	public int delete(@NonNull Uri uri,
	                  @Nullable String selection,
	                  @Nullable String[] selectionArgs) {
		int                  match = uriMatcher.match(uri);
		final SQLiteDatabase db;
		int                  count;
		String               selectionCriteria;
		switch(match) {
		case TASKS:
			db = openHelper.getWritableDatabase();
			count = db.delete(Tasks.Table, selection, selectionArgs);
			break;
		case TASKS_ID:
			db = openHelper.getWritableDatabase();
			long taskId = Tasks.getId(uri);
			selectionCriteria = Tasks._id + " = " + taskId;
			if(selection != null && selection.length() > 0) {
				selectionCriteria += " AND (" + selection + ")";
			}
			count = db.delete(Tasks.Table, selectionCriteria, selectionArgs);
			break;

		case TIMINGS:
			db = openHelper.getWritableDatabase();
			count = db.delete(Timings.Table, selection, selectionArgs);
			break;
		case TIMINGS_ID:
			db = openHelper.getWritableDatabase();
			long timingsId = Timings.getId(uri);
			selectionCriteria = Timings._id + " = " + timingsId;
			if(selection != null && selection.length() > 0) {
				selectionCriteria += " AND (" + selection + ")";
			}
			count = db.delete(Timings.Table, selectionCriteria, selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		if(count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	@Override
	public int update(@NonNull Uri uri,
	                  @Nullable ContentValues values,
	                  @Nullable String selection,
	                  @Nullable String[] selectionArgs) {
		int                  match = uriMatcher.match(uri);
		final SQLiteDatabase db;
		int                  count;
		String               selectionCriteria;
		switch(match) {
		case TASKS:
			db = openHelper.getWritableDatabase();
			count = db.update(Tasks.Table, values, selection, selectionArgs);
			break;
		case TASKS_ID:
			db = openHelper.getWritableDatabase();
			long taskId = Tasks.getId(uri);
			selectionCriteria = Tasks._id + " = " + taskId;
			if(selection != null && selection.length() > 0) {
				selectionCriteria += " AND (" + selection + ")";
			}
			count = db.update(Tasks.Table, values, selectionCriteria, selectionArgs);
			break;

		case TIMINGS:
			db = openHelper.getWritableDatabase();
			count = db.update(Timings.Table, values, selection, selectionArgs);
			break;
		case TIMINGS_ID:
			db = openHelper.getWritableDatabase();
			long timingsId = Timings.getId(uri);
			selectionCriteria = Timings._id + " = " + timingsId;
			if(selection != null && selection.length() > 0) {
				selectionCriteria += " AND (" + selection + ")";
			}
			count = db.update(Timings.Table, values, selectionCriteria, selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		if(count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}
}
