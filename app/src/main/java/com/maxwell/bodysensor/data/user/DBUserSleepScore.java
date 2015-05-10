package com.maxwell.bodysensor.data.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DBDailyRecord;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DBSleepLog;
import com.maxwell.bodysensor.data.DBSleepScore;
import com.maxwell.bodysensor.data.DSleepData;
import com.maxwell.bodysensor.data.SleepLogData;
import com.maxwell.bodysensor.data.SleepMoveData;
import com.maxwell.bodysensor.data.SleepScore;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;

import java.util.List;

/**
 * Created by ryanhsueh on 15/5/6.
 */
public class DBUserSleepScore extends DBSleepScore {

    public static final String TABLE = "DBDailySleepScoreRecord";

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN._ID + " INTEGER PRIMARY KEY" + "," +
                COLUMN.DEVICE_MAC + " TEXT NOT NULL" + "," +
                COLUMN.DATE + " INTEGER" + "," +
                COLUMN.DURATION + " INTEGER" + "," +
                COLUMN.SCORE + " REAL" + "," +
                COLUMN.TIMESWOKE + " INTEGER" +
                ");");
    }

    private static DBUserSleepScore sManager = null;

    private DBUserSleepScore(SQLiteDatabase db, DBProgramData pData) {
        mDB = db;
        mPD = pData;
    }

    // Only be init By DBProgramData
    public static synchronized DBUserSleepScore getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db, DBProgramData pData) {
        if (sManager == null) {
            sManager = new DBUserSleepScore(db, pData);
        }
    }

    public void releaseInstance() {
        sManager = null;
    }

    public void updateSleepScoreRecord(Cursor cursor){
        while (cursor.moveToNext()){
            ContentValues cv = new ContentValues();
            cv.put(COLUMN._ID, cursor.getString(cursor.getColumnIndex(COLUMN._ID)));
            cv.put(COLUMN.DEVICE_MAC, cursor.getString(cursor.getColumnIndex(COLUMN.DEVICE_MAC)));
            cv.put(COLUMN.DATE, cursor.getString(cursor.getColumnIndex(COLUMN.DATE)));
            cv.put(COLUMN.DURATION, cursor.getString(cursor.getColumnIndex(COLUMN.DURATION)));
            cv.put(COLUMN.SCORE, cursor.getString(cursor.getColumnIndex(COLUMN.SCORE)));
            cv.put(COLUMN.TIMESWOKE, cursor.getString(cursor.getColumnIndex(COLUMN.TIMESWOKE)));
            mDB.insert(TABLE, null, cv);
        }

    }

    public void updateDSleepData(UtilCalendar date, String deviceMac) {
        DBUserSleepLog sleepLogMgr = DBUserSleepLog.getInstance();

        int iSize = sleepLogMgr.querySleepLogSize(date, deviceMac);
        String strSelection = COLUMN.DEVICE_MAC+"=\""+deviceMac+"\"" + " AND " +
                COLUMN.DATE+"=\""+Long.toString(date.getUnixTime())+"\"";

        if (iSize==0) {
            int iNum = mDB.delete(TABLE, strSelection, null);
            UtilDBG.i("updateDSleepData, deleted rows: " + Integer.toString(iNum));
        } else {
            SleepLogData log = sleepLogMgr.querySleepLogLongest(date, deviceMac);
            SleepScore score = calculateSleepScore(date, log.mStartTime, log.mStopTime, deviceMac);

            updateDSleepData(TABLE, strSelection, date, score, deviceMac);
        }
    }

    public SleepScore calculateSleepScore(UtilCalendar date, UtilCalendar startTime, UtilCalendar stopTime, String deviceMac) {
        DBUser15MinutesRecord moveRecordMgr = DBUser15MinutesRecord.getInstance();
        List<SleepMoveData> sleepMoves = moveRecordMgr.querySleepMoveData(startTime, stopTime, deviceMac);

        return calculateSleepScore(date, startTime, stopTime, sleepMoves);
    }

    public List<DSleepData> queryDSleepData(UtilCalendar begin, UtilCalendar end, String deviceMac) {
        return queryDSleepData(TABLE, begin, end, deviceMac);
    }
}
