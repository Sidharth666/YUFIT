package com.maxwell.bodysensor.data.group;

import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DBProgramData;
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
public class DBGroupSleepScore extends DBSleepScore {

    public static final String TABLE = "DBDailySleepScoreRecord_Group";

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

    private static DBGroupSleepScore sManager = null;

    private DBGroupSleepScore(SQLiteDatabase db, DBProgramData pData) {
        mDB = db;
        mPD = pData;
    }

    // Only be init By DBProgramData
    public static synchronized DBGroupSleepScore getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db, DBProgramData pData) {
        if (sManager == null) {
            sManager = new DBGroupSleepScore(db, pData);
        }
    }

    public void releaseInstance() {
        sManager = null;
    }

    public void updateDSleepData(UtilCalendar date, String deviceMac) {
        DBGroupSleepLog sleepLogMgr = DBGroupSleepLog.getInstance();

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
        DBGroup15MinutesRecord moveRecordMgr = DBGroup15MinutesRecord.getInstance();
        List<SleepMoveData> sleepMoves = moveRecordMgr.querySleepMoveData(startTime, stopTime, deviceMac);

        return calculateSleepScore(date, startTime, stopTime, sleepMoves);
    }

    public List<DSleepData> queryDSleepData(UtilCalendar begin, UtilCalendar end, String deviceMac) {
        return queryDSleepData(TABLE, begin, end, deviceMac);
    }
}
