package com.maxwell.bodysensor.data.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DBDailyRecord;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DailyRecordData;
import com.maxwell.bodysensor.data.HourlyRecordData;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilTZ;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ryanhsueh on 15/5/6.
 */
public class DBUserDailyRecord extends DBDailyRecord {

    public static final String TABLE = "DBDailyRecord";

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN._ID + " INTEGER PRIMARY KEY" + "," +
                COLUMN.DEVICE_MAC + " TEXT NOT NULL" + "," +
                COLUMN.DATE + " INTEGER" + "," +
                COLUMN.STEP + " INTEGER" + "," +
                COLUMN.APP_ENERGY + " REAL" + "," +
                COLUMN.CALORIES + " REAL" + "," +
                COLUMN.DISTANCE + " REAL" + "," +
                COLUMN.GOAL + " INTEGER" +
                ");");
    }

    private static DBUserDailyRecord sManager = null;

    private DBUserDailyRecord(SQLiteDatabase db, DBProgramData pData) {
        mDB = db;
        mPD = pData;
    }

    public void updateDailyRecord(Cursor cursor){

        while (cursor.moveToNext()){
            ContentValues cv = new ContentValues();
            cv.put(COLUMN._ID, cursor.getString(cursor.getColumnIndex(COLUMN._ID)));
            cv.put(COLUMN.DEVICE_MAC, cursor.getString(cursor.getColumnIndex(COLUMN.DEVICE_MAC)));
            cv.put(COLUMN.DATE, cursor.getString(cursor.getColumnIndex(COLUMN.DATE)));
            cv.put(COLUMN.STEP, cursor.getString(cursor.getColumnIndex(COLUMN.STEP)));
            cv.put(COLUMN.APP_ENERGY, cursor.getString(cursor.getColumnIndex(COLUMN.APP_ENERGY)));
            cv.put(COLUMN.CALORIES, cursor.getString(cursor.getColumnIndex(COLUMN.CALORIES)));
            cv.put(COLUMN.DISTANCE, cursor.getString(cursor.getColumnIndex(COLUMN.DISTANCE)));
            cv.put(COLUMN.GOAL, cursor.getString(cursor.getColumnIndex(COLUMN.GOAL)));
            mDB.insert(TABLE, null, cv);
        }

    }

    // Only be init By DBProgramData
    public static synchronized DBUserDailyRecord getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db, DBProgramData pData) {
        if (sManager == null) {
            sManager = new DBUserDailyRecord(db, pData);
        }
    }

    public void releaseInstance() {
        sManager = null;
    }


    public void saveDailyRecord(Date date, int energy, int step, String deviceMac) {
        double dStride = mPD.getPersonStride();
        double dWeight = mPD.getPersonWeight();
        int iGoal = mPD.getPersonGoal();

        saveDailyRecord(TABLE, date, energy, step, deviceMac, dStride, dWeight, iGoal);
    }

    public List<DailyRecordData> queryDailyData(UtilCalendar begin, UtilCalendar end, UtilCalendar today, String deviceMac) {
        List<DailyRecordData> list = queryDailyData(TABLE, begin, end, today, deviceMac);

        if (today.compareTo(begin)>=0 &&
                today.compareTo(end)<=0) {
            DailyRecordData data = getDailyDataByHourlyRecord(today, deviceMac);
            if (data!=null) {
                list.add(data);
            }
        }

        return list;
    }

    private DailyRecordData getDailyDataByHourlyRecord(UtilCalendar date, String deviceMac) {
        UtilCalendar localTime = new UtilCalendar(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH)+1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                UtilTZ.getDefaultTZ());

        DBUserHourlyRecord hourRecordMgr = DBUserHourlyRecord.getInstance();
        List<HourlyRecordData> dataRecords = hourRecordMgr.queryHourlyData(
                localTime.getFirstSecondBDay(), localTime.getLastSecondBDay(), deviceMac);

        return createDailyDataByHourlyRecord(date, dataRecords, mPD.getPersonGoal());
    }

    public boolean deleteDailyRecords(String address) {
        return deleteRecords(TABLE, address);
    }
}
