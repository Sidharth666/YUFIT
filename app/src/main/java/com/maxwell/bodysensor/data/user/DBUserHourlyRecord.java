package com.maxwell.bodysensor.data.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DBHourlyRecord;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.HourlyRecordData;
import com.maxwell.bodysensor.util.UtilCalendar;

import java.util.Date;
import java.util.List;

/**
 * Created by ryanhsueh on 15/5/6.
 */
public class DBUserHourlyRecord extends DBHourlyRecord {

    public static final String TABLE = "DBHourlyRecord";

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN._ID + " INTEGER PRIMARY KEY" + "," +
                COLUMN.DEVICE_MAC + " TEXT NOT NULL" + "," +
                COLUMN.DATE + " INTEGER" + "," +
                COLUMN.STEP + " INTEGER" + "," +
                COLUMN.APP_ENERGY + " REAL" + "," +
                COLUMN.CALORIES + " REAL" + "," +
                COLUMN.DISTANCE + " REAL" + "," +
                COLUMN.LOG_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP " + // GMT
                ");");
    }

    private static DBUserHourlyRecord sManager = null;

    private DBUserHourlyRecord(SQLiteDatabase db, DBProgramData pData) {
        mDB = db;
        mPD = pData;
    }

    // Only be init By DBProgramData
    public static synchronized DBUserHourlyRecord getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db, DBProgramData pData) {
        if (sManager == null) {
            sManager = new DBUserHourlyRecord(db, pData);
        }
    }

    public void releaseInstance() {
        sManager = null;
    }

    public void updateHourlyRecord(Cursor cursor){
        while (cursor.moveToNext()){
            ContentValues cv = new ContentValues();
            cv.put(COLUMN._ID, cursor.getString(cursor.getColumnIndex(COLUMN._ID)));
            cv.put(COLUMN.DEVICE_MAC, cursor.getString(cursor.getColumnIndex(COLUMN.DEVICE_MAC)));
            cv.put(COLUMN.DATE, cursor.getString(cursor.getColumnIndex(COLUMN.DATE)));
            cv.put(COLUMN.STEP, cursor.getString(cursor.getColumnIndex(COLUMN.STEP)));
            cv.put(COLUMN.APP_ENERGY, cursor.getString(cursor.getColumnIndex(COLUMN.APP_ENERGY)));
            cv.put(COLUMN.CALORIES, cursor.getString(cursor.getColumnIndex(COLUMN.CALORIES)));
            cv.put(COLUMN.DISTANCE, cursor.getString(cursor.getColumnIndex(COLUMN.DISTANCE)));
            mDB.insertWithOnConflict(TABLE, null, cv,SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public void saveHourlyRecord(Date date, int energy, int step, String deviceMac, int iMinuteOffset) {
        double dStride = mPD.getPersonStride();
        double dWeight = mPD.getPersonWeight();

        saveHourlyRecord(TABLE, date, energy, step, deviceMac, iMinuteOffset, dStride, dWeight);
    }

    public List<HourlyRecordData> queryHourlyData(UtilCalendar begin, UtilCalendar end, String deviceMac) {
        return queryHourlyRecord(TABLE, begin, end, deviceMac);
    }

    public boolean deleteHourlyRecords(String address) {
        return deleteRecords(TABLE, address);
    }
}
