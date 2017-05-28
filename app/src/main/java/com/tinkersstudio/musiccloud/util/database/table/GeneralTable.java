package com.tinkersstudio.musiccloud.util.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Owner on 5/15/2017.
 */

public class GeneralTable {

    /** General table in the database. This count the user listening pattern based on the number of
     * time user listen to certain aspect*/
    public static final String DATABASE_TABLE_GENERAL = "general_table";

    /** Joke table column names and IDs for database access. */
    public static final String SONG_KEY_ID = "_id";
    public static final int SONG_COL_ID = 0;

    public static final String SONG_KEY_GENRE = "song_genre";
    public static final int SONG_COL_GENRE = SONG_COL_ID + 1;

    public static final String SONG_KEY_COUNT = "count";
    public static final int SONG_COL_COUNT = SONG_COL_ID + 2;


    /** SQLite database creation statement. Auto-increments IDs of inserted jokes.
     * Joke IDs are set after insertion into the database. */
    public static final String DATABASE_CREATE = "create table " + DATABASE_TABLE_GENERAL + " (" +
            SONG_KEY_ID + " integer primary key autoincrement, " +
            SONG_KEY_GENRE	+ " text not null, " +
            SONG_KEY_COUNT	+ " integer not null );";

    /** SQLite database table removal statement. Only used if upgrading database. */
    public static final String DATABASE_DROP = "drop table if exists " + DATABASE_TABLE_GENERAL;

    /**
     * Initializes the database.
     *
     * @param database
     * 				The database to initialize.
     */
    public static void onCreate(SQLiteDatabase database) {
		/* Create the table "joke_table" */
        database.execSQL(DATABASE_CREATE);
    }

    /**
     * Upgrades the database to a new version.
     *
     * @param database
     * 					The database to upgrade.
     * @param oldVersion
     * 					The old version of the database.
     * @param newVersion
     * 					The new version of the database.
     */
    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(GeneralTable.class.getName(), "The Database is being " +
                "updated from old version: " + oldVersion +
                " to a new version: " + newVersion);
		/* Drop the table */
        database.execSQL(DATABASE_DROP);

		/* Create a new table*/
        onCreate(database);

    }
}
