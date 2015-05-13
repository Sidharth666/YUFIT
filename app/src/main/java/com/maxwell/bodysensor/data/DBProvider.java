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

    private static final int USER_15_MIN_TABLE = 1;
    private static final int USER_DAILY_REC_TABLE = 2;
    private static final int USER_DEVICE_TABLE = 3;
    private static final int USER_HOURLY_TABLE = 4;
    private static final int USER_PROFILE_TABLE = 5;
    private static final int USER_SLEEP_LOG_TABLE = 6;
    private static final int USER_SLEEP_SCORE_TABLE = 7;

    public DBProvider() {
    }

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, DBUSER15MINTABLE, USER_15_MIN_TABLE);
        uriMatcher.addURI(AUTHORITY, DBUSERDAILYTABLE, USER_DAILY_REC_TABLE);
        uriMatcher.addURI(AUTHORITY, DBUSERDEVICETABLE, USER_DEVICE_TABLE);
        uriMatcher.addURI(AUTHORITY, DBUSERHOURLYTABLE, USER_HOURLY_TABLE);
        uriMatcher.addURI(AUTHORITY, DBUSERPROFILETABLE, USER_PROFILE_TABLE);
        uriMatcher.addURI(AUTHORITY, DBUSERSLEEPLOGTABLE, USER_SLEEP_LOG_TABLE);
        uriMatcher.addURI(AUTHORITY, DBUSER1SLEEPSCORETABLE, USER_SLEEP_SCORE_TABLE);
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

        switch (uriMatcher.match(uri)) {
            case USER_15_MIN_TABLE:
                mDB.insert(DBUSER15MINTABLE, null, values);
                break;
            case USER_DAILY_REC_TABLE:
                mDB.insert(DBUSERDAILYTABLE, null, values);
                break;
            case USER_DEVICE_TABLE:
                mDB.insert(DBUSERDEVICETABLE, null, values);
                break;
            case USER_HOURLY_TABLE:
                mDB.insert(DBUSERHOURLYTABLE, null, values);
                break;
            case USER_PROFILE_TABLE:
                mDB.insert(DBUSERPROFILETABLE, null, values);
                break;
            case USER_SLEEP_LOG_TABLE:
                mDB.insert(DBUSERSLEEPLOGTABLE, null, values);
                break;
            case USER_SLEEP_SCORE_TABLE:
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
        switch (uriMatcher.match(uri)) {
            case USER_15_MIN_TABLE:
                queryBuilder.setTables(DBUSER15MINTABLE);
                break;
            case USER_DAILY_REC_TABLE:
                queryBuilder.setTables(DBUSERDAILYTABLE);
                break;
            case USER_DEVICE_TABLE:
                queryBuilder.setTables(DBUSERDEVICETABLE);
                break;
            case USER_HOURLY_TABLE:
                queryBuilder.setTables(DBUSERHOURLYTABLE);
                break;
            case USER_PROFILE_TABLE:
                queryBuilder.setTables(DBUSERPROFILETABLE);
                break;
            case USER_SLEEP_LOG_TABLE:
                queryBuilder.setTables(DBUSERSLEEPLOGTABLE);
                break;
            case USER_SLEEP_SCORE_TABLE:
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
