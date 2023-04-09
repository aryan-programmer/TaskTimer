package com.aryanstein.mcad.tasktimer.db;

import android.content.ContentUris;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

public class Timings {
	public static final String Table     = "Timings";
	public static final String _id       = BaseColumns._ID;
	public static final String TaskId    = "TaskId";
	public static final String StartTime = "StartTime";
	public static final String Duration  = "Duration";

	/*
CREATE TABLE Timings (
_id INTEGER PRIMARY KEY NOT NULL,
TaskId INTEGER NOT NULL,
StartTime INTEGER,
Duration INTEGER
);

CREATE TRIGGER RemoveTask
AFTER DELETE
ON Tasks
FOR EACH ROW
BEGIN
DELETE FROM Timings
WHERE TaskId = OLD._id;
END;
	 */
	static final String CREATE_TABLE               =
		"CREATE TABLE " + Table + " (" +
			/**/_id /*      */ + " INTEGER PRIMARY KEY NOT NULL, " +
			/**/TaskId /*   */ + " INTEGER " + /*  */ "NOT NULL, " +
			/**/StartTime /**/ + " INTEGER, " +
			/**/Duration /* */ + " INTEGER" +
		");";
	static final String CREATE_REMOVE_TASK_TRIGGER =
		"CREATE TRIGGER RemoveTask " +
			/**/"AFTER DELETE " +
			/**/"ON " + Tasks.Table + " " +
			/**/"FOR EACH ROW " +
		"BEGIN " +
			/**/"DELETE FROM " + Table + " " +
			/**/"WHERE " + TaskId + " = OLD." + _id + "; " +
		"END;";

	static void create(SQLiteDatabase db){
		db.execSQL(CREATE_TABLE);
		db.execSQL(CREATE_REMOVE_TASK_TRIGGER);
	}

	public static final Uri CONTENT_URI =
		Uri.withAppendedPath(AppProvider.CONTENT_AUTHORITY_URI, Table);

	static final String CONTENT_TYPE      =
		"vnd.android.cursor.dir/vnd." + AppProvider.CONTENT_AUTHORITY + "." + Table;
	static final String CONTENT_ITEM_TYPE =
		"vnd.android.cursor.item/vnd." + AppProvider.CONTENT_AUTHORITY + "." + Table;

	public static Uri buildUri(long taskId) {
		return ContentUris.withAppendedId(CONTENT_URI, taskId);
	}

	public static long getId(Uri uri) {
		return ContentUris.parseId(uri);
	}

	private Timings() {
	}
}
