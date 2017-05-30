package com.tinkersstudio.musiccloud.deprecated.cursor;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.tinkersstudio.musiccloud.deprecated.DatabaseHelper;

/**
 * Created by Owner on 4/27/2017.
 */

public class SongContentProvider extends ContentProvider{

    /** The joke database. */
    private DatabaseHelper database;

    /** The database table to read from and write to, and also the root path for use in the URI matcher.
     * This is essentially a label to a two-dimensional array in the database filled with rows of jokes
     * whose columns contain joke data. */
    private static final String BASE_PATH = "general_table";

    /** Values for the URIMatcher. */
    private static final int JOKE_ID = 1;
    private static final int JOKE_FILTER = 2;

    /** The authority for this content provider. */
    private static final String AUTHORITY = "com.tinkersstudio.musiccloud.deprecated.cursor.contentprovider";


    /** This provider's content location. Used by accessing applications to
     * interact with this provider. */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    /** Matches content URIs requested by accessing applications with possible
     * expected content URI formats to take specific actions in this provider. */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //FIXME: Using custom databse
    //no modifier
    static {
        //search with jokeID
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/jokes/#", JOKE_ID);
        //for joke filter
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/filters/#", JOKE_FILTER);
    }

    @Override
    public boolean onCreate() {
		/* Create a database */
        database = new DatabaseHelper(getContext(), DatabaseHelper.getDatabase(),
                null, DatabaseHelper.getVersion());
        return true;
    }

    /**
     * Fetches rows from the joke table. Given a specified URI that contains a
     * filter, returns a list of jokes from the joke table matching that filter in the
     * form of a Cursor.<br><br>
     *
     * Overrides the built-in version of <b>query(...)</b> provided by ContentProvider.<br><br>
     *
     * For more information, read the documentation for the built-in version of this
     * method by hovering over the method name in the method signature below this
     * comment block in Eclipse and clicking <b>query(...)</b> in the Overrides details.
     * */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
		/* Use a helper class to perform a query for us */
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		/* Make sure the projection is proper before querying */
        checkColumns(projection);

		/* Set up helper to query our jokes table */
        //queryBuilder.setTables(JokeTable.DATABASE_TABLE_JOKE);

		/* Match the passed-in URI to an expected URI format */
        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case JOKE_FILTER:
                /* Fetch the last segment of the URI,which should be a filter number */
                String filter = uri.getLastPathSegment();
                /** Leave selection as null to fetch all rows if filter is Show All. Otherwise,
                 * fetch rows with a specific rating according to the parsed filter. */
                /**
                if (!filter.equals(AdvancedJokeList.SHOW_ALL_FILTER_STRING)) {
                    queryBuilder.appendWhere(JokeTable.JOKE_KEY_RATING + "=" + filter);
                } else {
                    selection = null; //return all rows
                }
                 */
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        /* Perform the database query */
		/* Obtain the cursor in here */
        SQLiteDatabase db = this.database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, null, null, null, null);

		/* Set the cursor to automatically alert listeners for content/view refreshing */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /** We don't really care about this method for this application. */
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * Inserts a joke into the joke table. Given a specific URI that contains a
     * joke and the values of that joke, writes a new row in the table filled
     * with that joke's information and gives the joke a new ID, then returns a URI
     * containing the ID of the inserted joke.<br><br>
     *
     * Overrides the built-in version of <b>insert(...)</b> provided by ContentProvider.<br><br>
     *
     * For more information, read the documentation for the built-in version of this
     * method by hovering over the method name in the method signature below this
     * comment block in Eclipse and clicking <b>insert(...)</b> in the Overries details.
     * */
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        /** Open the database for writing. */
        SQLiteDatabase sqlDB = this.database.getWritableDatabase();

        /** Will contain the ID of the inserted joke. */
        long id = 0;

        /** Match the passed-in URI to an expected URI format. */
        int uriType = sURIMatcher.match(uri);

        switch(uriType)	{

            /** Expects a joke ID, but we will do nothing with the passed-in ID since
             * the database will automatically handle ID assignment and incrementation.
             * IMPORTANT: joke ID cannot be set to -1 in passed-in URI; -1 is not interpreted
             * as a numerical value by the URIMatcher. */
            case JOKE_ID:

                /** Perform the database insert, placing the joke at the bottom of the table. */
                //id = sqlDB.insert(JokeTable.DATABASE_TABLE_JOKE, null, values);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        /** Alert any watchers of an underlying data change for content/view refreshing. */
        getContext().getContentResolver().notifyChange(uri, null);

        return Uri.parse(BASE_PATH + "/" + id);
    }

    /**
     * Removes a row from the joke table. Given a specific URI containing a joke ID,
     * removes rows in the table that match the ID and returns the number of rows removed.
     * Since IDs are automatically incremented on insertion, this will only ever remove
     * a single row from the joke table.<br><br>
     *
     * Overrides the built-in version of <b>delete(...)</b> provided by ContentProvider.<br><br>
     *
     * For more information, read the documentation for the built-in version of this
     * method by hovering over the method name in the method signature below this
     * comment block in Eclipse and clicking <b>delete(...)</b> in the Overrides details.
     * */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        /** Open the database for writing. */
        SQLiteDatabase sqlDB = this.database.getWritableDatabase();
        int numRow = 0;

        /* Match URI format denoted by JOKE_ID */
        int uriType = sURIMatcher.match(uri);

        switch(uriType){
            case JOKE_ID:
                /* Fetch the last segment of the URI,which should be a filter number */
                String jokeID = uri.getLastPathSegment();

                //numRow = sqlDB.delete(JokeTable.DATABASE_TABLE_JOKE, JokeTable.JOKE_KEY_ID
                //        + "=" + jokeID, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (numRow > 0) {
            /** Notify components that attached to the joke table*/
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRow;
    }

    /**
     * Updates a row in the joke table. Given a specific URI containing a joke ID and the
     * new joke values, updates the values in the row with the matching ID in the table.
     * Since IDs are automatically incremented on insertion, this will only ever update
     * a single row in the joke table.<br><br>
     *
     * Overrides the built-in version of <b>update(...)</b> provided by ContentProvider.<br><br>
     *
     * For more information, read the documentation for the built-in version of this
     * method by hovering over the method name in the method signature below this
     * comment block in Eclipse and clicking <b>update(...)</b> in the Overrides details.
     * */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase sqLiteDatabase = this.database.getWritableDatabase();
        int numRow = 0;

        /* Match URI format denoted by JOKE_ID */
        int uriType = sURIMatcher.match(uri);

        switch(uriType){
            case JOKE_ID:

                /* Fetch the last segment of the URI,which should be a filter number */
                String jokeID = uri.getLastPathSegment();

                //numRow = sqLiteDatabase.update(JokeTable.DATABASE_TABLE_JOKE, values, JokeTable.JOKE_KEY_ID + "=" + jokeID, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (numRow > 0) {
            /** Alert changes*/
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRow;
    }

    /**
     * Verifies the correct set of columns to return data from when performing a query.
     *
     * @param projection
     * 						The set of columns about to be queried.
     */
    private void checkColumns(String[] projection) {
        /*
        //String[] available = { JokeTable.JOKE_KEY_ID, JokeTable.JOKE_KEY_TEXT, JokeTable.JOKE_KEY_RATING,
        //        JokeTable.JOKE_KEY_AUTHOR };

        if(projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));

            if(!availableColumns.containsAll(requestedColumns))	{
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
        */
    }
}
