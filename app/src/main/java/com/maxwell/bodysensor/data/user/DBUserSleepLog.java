package com.maxwell.bodysensor.data.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DBDailyRecord;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DBSleepLog;
import com.maxwell.bodysensor.data.SleepLogData;
import com.maxwell.bodysensor.util.UtilCalendar;

import java.util.List;

/**
 * Created by ryanhsueh on 15/5/6.
 */
public class DBUserSleepLog extends DBSleepLog {

    public static final String TABLE = "DBSleepLog";

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN._ID + " INTEGER PRIMARY KEY" + "," +
                COLUMN.DEVICE_MAC + " TEXT NOT NULL" + "," +
                COLUMN.DATE + " INTEGER" + "," +
                COLUMN.START_TIME + " INTEGER" + "," +
                COLUMN.STOP_TIME + " INTEGER" +
                ");");
    }

    private static DBUserSleepLog mManager = null;

    private DBUserSleepLog(SQLiteDatabase db, DBProgramData pData) {
        mDB = db;
        mPD = pData;
    }

    // Only be init By DBProgramData
    public static synchronized DBUserSleepLog getInstance() {
        return mManager;
    }

    public static void init(SQLiteDatabase db, DBProgramData pData) {
        if (mManager == null) {
            mManager = new DBUserSleepLog(db, pData);
        }
    }

    public void releaseInstance() {
        mManager = null;
    }

    public void updateSleepLogRecord(Cursor cursor){
        while (cursor.moveToNext()){
            ContentValues cv = new ContentValues();
            cv.put(COLUMN._ID, cursor.getString(cursor.getColumnIndex(DBDailyRecord.COLUMN._ID)));
            cv.put(COLUMN.DEVICE_MAC, cursor.getString(cursor.getColumnIndex(DBDailyRecord.COLUMN.DEVICE_MAC)));
            cv.put(COLUMN.DATE, cursor.getString(cursor.getColumnIndex(DBDailyRecord.COLUMN.DATE)));
            cv.put(COLUMN.START_TIME, cursor.getString(cursor.getColumnIndex(DBDailyRecord.COLUMN.STEP)));
            cv.put(COLUMN.STOP_TIME, cursor.getString(cursor.getColumnIndex(DBDailyRecord.COLUMN.APP_ENERGY)));
            mDB.insert(TABLE, null,cv);
        }

    }

    public int addSleepLog(UtilCalendar date, UtilCalendar start, UtilCalendar stop, String deviceMac) {
        int iReturn = addSleepLog(TABLE, date, start, stop, deviceMac);

        DBUserSleepScore sleepScoreMgr = DBUserSleepScore.getInstance();
        sleepScoreMgr.updateDSleepData(date, deviceMac);

        return iReturn;
    }

    public int querySleepLogSize(UtilCalendar date, String deviceMac) {
        return querySleepLogSize(TABLE, date, deviceMac);
    }

    public SleepLogData querySleepLog(UtilCalendar date, int iIndex, String deviceMac) {
        return querySleepLog(TABLE, date, iIndex, deviceMac);
    }

    public void deleteSleepLog(UtilCalendar date, int iIndex, String deviceMac) {
        deleteSleepLog(TABLE, date, iIndex, deviceMac);

        DBUserSleepScore sleepScoredMgr = DBUserSleepScore.getInstance();
        sleepScoredMgr.updateDSleepData(date, deviceMac);
    }

    public SleepLogData querySleepLogLongest(UtilCalendar date, String deviceMac) {
        return querySleepLogLongest(TABLE, date, deviceMac);
    }

    private List<SleepLogData> getSleepLogList(UtilCalendar date, String deviceMac) {
        return getSleepLogList(TABLE, date, deviceMac);
    }
}
