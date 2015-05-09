package com.maxwell.bodysensor.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.maxwell.bodysensor.data.user.DBUser15MinutesRecord;
import com.maxwell.bodysensor.data.user.DBUserDailyRecord;
import com.maxwell.bodysensor.data.user.DBUserDevice;
import com.maxwell.bodysensor.data.user.DBUserHourlyRecord;
import com.maxwell.bodysensor.data.user.DBUserProfile;
import com.maxwell.bodysensor.data.user.DBUserSleepLog;
import com.maxwell.bodysensor.data.user.DBUserSleepScore;

public class DBProvider extends ContentProvider {

    // database
    private DBProgramData mPD = null;
    private SQLiteDatabase mDB;
    private static final String AUTHORITY = "com.mxx.bodysensor.data.DBProvider";

    public static final String DB_NAME = "bs.db";
    public static final String DBUSER15MINTABLE = DBUser15MinutesRecord.TABLE;
    public static final String DBUSERDAILYTABLE = DBUserDailyRecord.TABLE;
    public static final String DBUSERDEVICETABLE = DBUserDevice.TABLE;
    public static final String DBUSERHOURLYTABLE = DBUserHourlyRecord.TABLE;
    public static final String DBUSERPROFILETABLE = DBUserProfile.TABLE;
    public static final String DBUSERSLEEPLOGTABLE = DBUserSleepLog.TABLE;
    public static final String DBUSER1SLEEPSCORETABLE = DBUserSleepScore.TABLE;

//    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);



    public DBProvider() {

    }

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, DBUSER15MINTABLE, 1);
        uriMatcher.addURI(AUTHORITY, DBUSERDAILYTABLE, 2);
        uriMatcher.addURI(AUTHORITY, DBUSERDEVICETABLE, 3);
        uriMatcher.addURI(AUTHORITY, DBUSERHOURLYTABLE, 4);
        uriMatcher.addURI(AUTHORITY, DBUSERPROFILETABLE, 5);
        uriMatcher.addURI(AUTHORITY, DBUSERSLEEPLOGTABLE, 6);
        uriMatcher.addURI(AUTHORITY, DBUSER1SLEEPSCORETABLE, 7);

    }



    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        switch (uriMatcher.match(uri)){
            case 1:
                mDB.insert(DBUSER15MINTABLE, null, values);
                break;
            case 2:
                mDB.insert(DBUSERDAILYTABLE, null, values);
                break;
            case 3:
                mDB.insert(DBUSERDEVICETABLE, null, values);
                break;
            case 4:
                mDB.insert(DBUSERHOURLYTABLE, null, values);
                break;
            case 5:
                mDB.insert(DBUSERPROFILETABLE, null, values);
                break;
            case 6:
                mDB.insert(DBUSERSLEEPLOGTABLE, null, values);
                break;
            case 7:
                mDB.insert(DBUSER1SLEEPSCORETABLE, null, values);
                break;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        DBProgramData.initInstance(getContext());
        mPD = DBProgramData.getInstance();
        mDB = mPD.getWritableDatabase();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)){
            case 1:
                queryBuilder.setTables(DBUSER15MINTABLE);
                break;
            case 2:
                queryBuilder.setTables(DBUSERDAILYTABLE);
                break;
            case 3:
                queryBuilder.setTables(DBUSERDEVICETABLE);
                break;
            case 4:
                queryBuilder.setTables(DBUSERHOURLYTABLE);
                break;
            case 5:
                queryBuilder.setTables(DBUSERPROFILETABLE);
                break;
            case 6:
                queryBuilder.setTables(DBUSERSLEEPLOGTABLE);
                break;
            case 7:
                queryBuilder.setTables(DBUSER1SLEEPSCORETABLE);
                break;
        }

        Cursor cursor = queryBuilder.query(mPD.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
