package com.aryanstein.mcad.tasktimer.db;

import android.content.ContentUris;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

public class Tasks {
	public static final String Table       = "Tasks";
	public static final String _id         = BaseColumns._ID;
	public static final String Name        = "Name";
	public static final String Description = "Description";
	public static final String SortOrder   = "SortOrder";

	/*
CREATE TABLE Tasks (
_id INTEGER PRIMARY KEY NOT NULL,
Name TEXT NOT NULL,
Description TEXT,
SortOrder INTEGER
);
*/
	private static final String CREATE_TABLE =
		"CREATE TABLE " + Table + " (" +
			/**/_id /*        */ + " INTEGER PRIMARY KEY NOT NULL, " +
			/**/Name /*       */ + " TEXT " + /*     */ "NOT NULL, " +
			/**/Description /**/ + " TEXT, " +
			/**/SortOrder /*  */ + " INTEGER" +
		");";

	static void create(SQLiteDatabase db){
		db.execSQL(CREATE_TABLE);
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

	private Tasks() {
	}
}
