package com.aryanstein.mcad.tasktimer.db;

import android.content.ContentUris;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

public class Durations {
	public static final String Table       = "VwTaskDurations";
	public static final String _id         = BaseColumns._ID;
	public static final String Name        = Tasks.Name;
	public static final String Description = Tasks.Description;
	public static final String StartTime   = Timings.StartTime;
	public static final String StartDate   = "StartDate";
	public static final String Duration    = Timings.Duration;

	private static final String Tasks__id         = Tasks.Table + "." + Tasks._id;
	private static final String Tasks_Name        = Tasks.Table + "." + Tasks.Name;
	private static final String Tasks_Description = Tasks.Table + "." + Tasks.Description;
	private static final String Timings__id       = Timings.Table + "." + Timings._id;
	private static final String Timings_TaskId    = Timings.Table + "." + Timings.TaskId;
	private static final String Timings_StartTime = Timings.Table + "." + Timings.StartTime;
	private static final String Timings_Duration  = Timings.Table + "." + Timings.Duration;

	/*
CREATE VIEW VwTaskDurations AS
SELECT Timings._id,
Tasks.Name,
Tasks.Description,
Timings.StartTime,
DATE(Timings.StartTime, 'unixepoch') AS StartDate,
SUM(Timings.Duration) AS Duration
FROM Tasks
INNER JOIN Timings ON Tasks._id = Timings.TaskId
GROUP BY Tasks._id, StartDate;
*/
	static final String CREATE_TABLE =
		"CREATE VIEW " + Table + " AS " +
		"SELECT " + Timings__id + ", " +
			/*   */ Tasks_Name + ", " +
			/*   */ Tasks_Description + ", " +
			/*   */ Timings_StartTime + ", " +
		"DATE(" + Timings_StartTime + ", 'unixepoch') AS " + StartDate + ", " +
		"SUM(" + Timings_Duration + /*          */ ") AS " + Duration + " " +
		"FROM " + Tasks.Table + " " +
			/**/ "INNER JOIN " + Timings.Table + " ON " + Tasks__id + " = " + Timings_TaskId + " " +
		"GROUP BY " + Tasks__id + ", " + StartDate + ";";

	static void create(SQLiteDatabase db){
		db.execSQL(CREATE_TABLE);
	}

	public static final Uri CONTENT_URI =
		Uri.withAppendedPath(AppProvider.CONTENT_AUTHORITY_URI, Table);

	static final String CONTENT_TYPE      =
		"vnd.android.cursor.dir/vnd." + AppProvider.CONTENT_AUTHORITY + "." + Table;
	static final String CONTENT_ITEM_TYPE =
		"vnd.android.cursor.item/vnd." + AppProvider.CONTENT_AUTHORITY + "." + Table;

	public static long getId(Uri uri) {
		return ContentUris.parseId(uri);
	}

	private Durations() {
	}
}
