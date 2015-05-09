package com.maxwell.bodysensor.data.group;

import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DBDevice;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DeviceData;

import java.util.ArrayList;

/**
 * Created by ryanhsueh on 15/5/6.
 */
public class DBGroupDevice extends DBDevice {

    public static final String TABLE = "DBDevice_Group";

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN._ID + " INTEGER PRIMARY KEY," +
                COLUMN.PROFILE_ID + " INTEGER" + "," +
                COLUMN.LAST_DAILY_SYNC + " INTEGER"  + "," +
                COLUMN.LAST_HOURLY_SYNC + " INTEGER"  + "," +
                COLUMN.TIMEZONE + " INTEGER" + "," +
                COLUMN.NAME + " TEXT NOT NULL" + "," +
                COLUMN.MAC + " TEXT UNIQUE" +
                ");");
    }

    private static DBGroupDevice sManager = null;

    private DBGroupDevice(SQLiteDatabase db, DBProgramData pData) {
        mDB = db;
        mPD = pData;
    }

    // Only be init By DBProgramData
    public static synchronized DBGroupDevice getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db, DBProgramData pData) {
        if (sManager == null) {
            sManager = new DBGroupDevice(db, pData);
            mListenerList = new ArrayList<OnDBDeviceUpdateListener>();
        }
    }

    public void releaseInstance() {
        sManager = null;

        mListenerList.clear();
        mListenerList = null;
    }

    public void updateDeviceData(DeviceData device) {
        updateDeviceData(TABLE, device);
    }

    public DeviceData getDeviceDataByAddress(String strAddress) {
        return getDeviceDataByAddress(TABLE, strAddress);
    }

    public int removeDevice(long member_id, String macAddress) {
        StringBuilder sb = new StringBuilder();
        sb.append(COLUMN.MAC).append("=\"").append(macAddress).append("\"")
                .append(" AND ").append(COLUMN.PROFILE_ID).append("=").append(member_id);

        return mDB.delete(TABLE, sb.toString(), null);
    }
}
