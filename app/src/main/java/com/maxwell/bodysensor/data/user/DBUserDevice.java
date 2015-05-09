package com.maxwell.bodysensor.data.user;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DBDevice;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DeviceData;
import com.maxwell.bodysensor.util.UtilDBG;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanhsueh on 15/5/6.
 */
public class DBUserDevice extends DBDevice {

    public static final String TABLE = "DBDevice";

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

    private static DBUserDevice sManager = null;

    private DBUserDevice(SQLiteDatabase db, DBProgramData pData) {
        mDB = db;
        mPD = pData;
    }

    // Only be init By DBProgramData
    public static synchronized DBUserDevice getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db, DBProgramData pData) {
        if (sManager == null) {
            sManager = new DBUserDevice(db, pData);
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

    public List<DeviceData> getDeviceList() {
        ArrayList<DeviceData> list = new ArrayList<DeviceData>();

        Cursor c = mDB.query(true, TABLE,
                new String[]{
                        COLUMN.PROFILE_ID,
                        COLUMN.LAST_DAILY_SYNC,
                        COLUMN.LAST_HOURLY_SYNC,
                        COLUMN.TIMEZONE,
                        COLUMN.NAME,
                        COLUMN.MAC,
                },
                null, null, null, null, null, null);
        while (c.moveToNext()) {
            DeviceData device = new DeviceData();

            device.profileId = mPD.getProfileId();
            device.lastDailySyncTime = c.getLong(c.getColumnIndex(COLUMN.LAST_DAILY_SYNC));
            device.lastHourlySyncTime = c.getLong(c.getColumnIndex(COLUMN.LAST_HOURLY_SYNC));
            device.lastTimezoneDiff = c.getInt(c.getColumnIndex(COLUMN.TIMEZONE));
            device.displayName = c.getString(c.getColumnIndex(COLUMN.NAME));
            device.mac = c.getString(c.getColumnIndex(COLUMN.MAC));

            list.add(device);
        }
        c.close();

        return list;
    }

    public void removeDevice(String strAddress) {
        Cursor c = mDB.query(true, TABLE,
                new String[]{
                        COLUMN.MAC,
                },
                COLUMN.MAC+"=\""+strAddress+"\"", null, null, null, null, null);
        int iCount = c.getCount();

        if (iCount!=0) {
            if (iCount>1) {
                UtilDBG.e("!! Assume that there is at most one row !!");
            } else {
                c.moveToFirst();
            }
        }

        c.close();

        int iNumDevice = mDB.delete(TABLE, COLUMN.MAC+"=\""+strAddress+"\"", null);
        UtilDBG.i("remove device from deviceList, address = " + strAddress
                + " result= " + Integer.toString(iNumDevice));
        if (iNumDevice!=1) {
            UtilDBG.e("removeDevice == unexpected == ");
        } else {
            // TODO : update focus device if need
            List<DeviceData> deviceList = getDeviceList();
            if (deviceList.size()==0) {
                mPD.setTargetDeviceMac("");
            } else {
                int iLatestIdx = 0;
                long latest = deviceList.get(0).lastHourlySyncTime;
                for (int i=1; i<deviceList.size(); ++i) {
                    long compare = deviceList.get(i).lastHourlySyncTime;
                    if (latest < compare) {
                        latest = compare;
                        iLatestIdx = i;
                    }
                }

                mPD.setTargetDeviceMac(deviceList.get(iLatestIdx).mac);
            }

            notifyUpdated();
        }
    }
}
