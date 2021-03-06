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
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id;
        Uri rowUri = null;
        switch (uriMatcher.match(uri)) {
            case USER_15_MIN_TABLE:
                id = mDB.insert(DBUSER15MINTABLE, null, values);
                rowUri = Uri.parse(DBUSER15MINTABLE + "/" + id);
                break;
            case USER_DAILY_REC_TABLE:
                id = mDB.insert(DBUSERDAILYTABLE, null, values);
                rowUri = Uri.parse(DBUSERDAILYTABLE + "/" + id);
                break;
            case USER_DEVICE_TABLE:
                id = mDB.insert(DBUSERDEVICETABLE, null, values);
                rowUri = Uri.parse(DBUSERDEVICETABLE + "/" + id);
                break;
            case USER_HOURLY_TABLE:
                id = mDB.insert(DBUSERHOURLYTABLE, null, values);
                rowUri = Uri.parse(DBUSERHOURLYTABLE + "/" + id);
                break;
            case USER_PROFILE_TABLE:
                id = mDB.insert(DBUSERPROFILETABLE, null, values);
                rowUri = Uri.parse(DBUSERPROFILETABLE + "/" + id);
                break;
            case USER_SLEEP_LOG_TABLE:
                id = mDB.insert(DBUSERSLEEPLOGTABLE, null, values);
                rowUri = Uri.parse(DBUSERSLEEPLOGTABLE + "/" + id);
                break;
            case USER_SLEEP_SCORE_TABLE:
                id = mDB.insert(DBUSER1SLEEPSCORETABLE, null, values);
                rowUri = Uri.parse(DBUSER1SLEEPSCORETABLE + "/" + id);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowUri;
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
        int rowsUpdated = 0;
        switch (uriMatcher.match(uri)) {
            case USER_15_MIN_TABLE:
                rowsUpdated = mDB.update(DBUSER15MINTABLE, values, null, null);
                break;
            case USER_DAILY_REC_TABLE:
                rowsUpdated = mDB.update(DBUSERDAILYTABLE, values, null, null);
                break;
            case USER_DEVICE_TABLE:
                rowsUpdated = mDB.update(DBUSERDEVICETABLE, values, null, null);
                break;
            case USER_HOURLY_TABLE:
                rowsUpdated = mDB.update(DBUSERHOURLYTABLE, values, null, null);
                break;
            case USER_PROFILE_TABLE:
                rowsUpdated = mDB.update(DBUSERPROFILETABLE, values, null, null);
                break;
            case USER_SLEEP_LOG_TABLE:
                rowsUpdated = mDB.update(DBUSERSLEEPLOGTABLE, values, null, null);
                break;
            case USER_SLEEP_SCORE_TABLE:
                rowsUpdated = mDB.update(DBUSER1SLEEPSCORETABLE, values, null, null);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
