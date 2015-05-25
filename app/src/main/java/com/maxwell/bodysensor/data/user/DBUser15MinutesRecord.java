package com.maxwell.bodysensor.data.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DB15MinutesRecord;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.SleepMoveData;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilTZ;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by ryanhsueh on 15/5/7.
 */
public class DBUser15MinutesRecord extends DB15MinutesRecord {

    public static final String TABLE = "DB15MinutesRecord";

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN._ID + " INTEGER PRIMARY KEY" + "," +
                COLUMN.DEVICE_MAC + " TEXT NOT NULL" + "," +
                COLUMN.DATE + " INTEGER" + "," +
                COLUMN.MOVE + " INTEGER" + "," +
                COLUMN.LOG_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP " +
                ");");
    }

    private static DBUser15MinutesRecord sManager = null;

    private DBUser15MinutesRecord(SQLiteDatabase db, DBProgramData pData) {
        mDB = db;
        mPD = pData;
    }

    // Only be init By DBProgramData
    public static synchronized DBUser15MinutesRecord getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db, DBProgramData pData) {
        if (sManager == null) {
            sManager = new DBUser15MinutesRecord(db, pData);
        }
    }

    public void releaseInstance() {
        sManager = null;
    }

    public void update15MinRecord(Cursor cursor){
        while (cursor.moveToNext()){
            ContentValues cv = new ContentValues();
            String rowId = cursor.getString(cursor.getColumnIndex(COLUMN._ID));
            cv.put(COLUMN._ID, rowId);
            cv.put(COLUMN.DEVICE_MAC, cursor.getString(cursor.getColumnIndex(COLUMN.DEVICE_MAC)));
            cv.put(COLUMN.DATE, cursor.getString(cursor.getColumnIndex(COLUMN.DATE)));
            cv.put(COLUMN.MOVE, cursor.getString(cursor.getColumnIndex(COLUMN.MOVE)));
            mDB.insertWithOnConflict(TABLE, null, cv,SQLiteDatabase.CONFLICT_REPLACE);
        }
    }


    public void save15MinBasedSleepMove(Date date, int move, String deviceMac, int iMinuteOffset) {
        boolean bBeginIsYesterday = false;

        // init, auto monitor
        int sleepBegin = mPD.getPersonSMBegin();
        int sleepEnd = mPD.getPersonSMEnd();
        int iBeginH = sleepBegin / 60;
        int iBeginM = sleepBegin % 60;
        int iEndH = sleepEnd / 60;
        int iEndM = sleepEnd % 60;

        if (sleepBegin > sleepEnd) {
            bBeginIsYesterday = true;
        }

        TimeZone tz = UtilTZ.getTZWithOffset(iMinuteOffset);

        UtilCalendar calDay = save15MinutesRecord(TABLE, date, move, deviceMac, tz,
                iBeginH, iEndH, bBeginIsYesterday);

        if (calDay != null) {
            UtilCalendar calStart = new UtilCalendar(
                    calDay.get(Calendar.YEAR),
                    calDay.get(Calendar.MONTH)+1,
                    calDay.get(Calendar.DAY_OF_MONTH),
                    iBeginH, iBeginM, 0, tz);
            UtilCalendar calStop = new UtilCalendar(
                    calDay.get(Calendar.YEAR),
                    calDay.get(Calendar.MONTH)+1,
                    calDay.get(Calendar.DAY_OF_MONTH),
                    iEndH, iEndM, 0, tz);

            if (bBeginIsYesterday) {
                calStart.add(Calendar.DAY_OF_MONTH, -1);
            }

            DBUserSleepLog sleepLogMgr = DBUserSleepLog.getInstance();
            sleepLogMgr.addSleepLog(calDay, calStart, calStop, deviceMac);
        }
    }

    public List<SleepMoveData> querySleepMoveData(UtilCalendar start, UtilCalendar stop, String deviceMac) {
        return querySleepMoveData(TABLE, start, stop, deviceMac);
    }
}
