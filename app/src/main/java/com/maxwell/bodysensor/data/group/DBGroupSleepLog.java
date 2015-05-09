package com.maxwell.bodysensor.data.group;

import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DBSleepLog;
import com.maxwell.bodysensor.data.SleepLogData;
import com.maxwell.bodysensor.util.UtilCalendar;

import java.util.List;

/**
 * Created by ryanhsueh on 15/5/6.
 */
public class DBGroupSleepLog extends DBSleepLog {

    public static final String TABLE = "DBSleepLog_Group";

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN._ID + " INTEGER PRIMARY KEY" + "," +
                COLUMN.DEVICE_MAC + " TEXT NOT NULL" + "," +
                COLUMN.DATE + " INTEGER" + "," +
                COLUMN.START_TIME + " INTEGER" + "," +
                COLUMN.STOP_TIME + " INTEGER" +
                ");");
    }

    private static DBGroupSleepLog sManager = null;

    private DBGroupSleepLog(SQLiteDatabase db, DBProgramData pData) {
        mDB = db;
        mPD = pData;
    }

    // Only be init By DBProgramData
    public static synchronized DBGroupSleepLog getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db, DBProgramData pData) {
        if (sManager == null) {
            sManager = new DBGroupSleepLog(db, pData);
        }
    }

    public void releaseInstance() {
        sManager = null;
    }

    public int addSleepLog(UtilCalendar date, UtilCalendar start, UtilCalendar stop, String deviceMac) {
        int iReturn = addSleepLog(TABLE, date, start, stop, deviceMac);

        DBGroupSleepScore sleepScoreMgr = DBGroupSleepScore.getInstance();
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

        DBGroupSleepScore sleepScoredMgr = DBGroupSleepScore.getInstance();
        sleepScoredMgr.updateDSleepData(date, deviceMac);
    }

    public SleepLogData querySleepLogLongest(UtilCalendar date, String deviceMac) {
        return querySleepLogLongest(TABLE, date, deviceMac);
    }

    private List<SleepLogData> getSleepLogList(UtilCalendar date, String deviceMac) {
        return getSleepLogList(TABLE, date, deviceMac);
    }

}
