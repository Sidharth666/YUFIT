package com.maxwell.bodysensor.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.util.UtilDBG;

import java.util.ArrayList;

/**
 * Created by ryanhsueh on 15/5/5.
 */
public abstract class DBDevice {

    public class COLUMN {
        public static final String _ID = "_id";
        public static final String PROFILE_ID = "profileId";
        public static final String LAST_DAILY_SYNC = "lastDailySyncDate";
        public static final String LAST_HOURLY_SYNC = "lastHourlySyncDate";
        public static final String TIMEZONE = "timezone";
        public static final String NAME = "name";              // the name, defined by the user.
        public static final String MAC = "mac";
    }

    protected SQLiteDatabase mDB;
    protected DBProgramData mPD;

    protected static ArrayList<OnDBDeviceUpdateListener> mListenerList;
    public interface OnDBDeviceUpdateListener {
        public void OnDBDeviceUpdated();
    }
    public void addListener(OnDBDeviceUpdateListener listener) {
        if (mListenerList != null && !mListenerList.contains(listener)) {
            mListenerList.add(listener);
        }
    }
    public void removeListener(OnDBDeviceUpdateListener listener) {
        if (mListenerList != null && mListenerList.contains(listener)) {
            mListenerList.remove(listener);
        }
    }
    protected void notifyUpdated() {
        if (mListenerList != null) {
            for (OnDBDeviceUpdateListener listener : mListenerList) {
                listener.OnDBDeviceUpdated();
            }
        }
    }

    protected void updateDeviceData(String table, DeviceData device) {
        if (device==null) {
            UtilDBG.e("updateDeviceData, parameter");
        }

        String selection = COLUMN.MAC+"=\""+device.mac+"\"";

        ContentValues cv = new ContentValues();

        cv.put(COLUMN.PROFILE_ID, device.profileId);
        cv.put(COLUMN.LAST_DAILY_SYNC, device.lastDailySyncTime);
        cv.put(COLUMN.LAST_HOURLY_SYNC, device.lastHourlySyncTime);
        cv.put(COLUMN.TIMEZONE, device.lastTimezoneDiff);
        cv.put(COLUMN.NAME, device.displayName);
        cv.put(COLUMN.MAC, device.mac);

        Cursor c = null;
        try {
            c = mDB.query(true, table,
                    new String[]{COLUMN.MAC},
                    selection, null, null, null, null, null);
            int iCount = c.getCount();

            // insert if not exist, update if exist
            if (iCount==0) {
                // insert
                long l = mDB.insert(table, null, cv);
                if (l > 0) {
                    notifyUpdated();
                }
            } else {
                if (iCount>1) {
                    UtilDBG.e("!! Assume that there is at most one row !!");
                }
                c.moveToFirst();

                int rows = mDB.update(table, cv, selection, null);
                if (rows > 0) {
                    notifyUpdated();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }

    }

    protected DeviceData getDeviceDataByAddress(String table, String strAddress) {
        DeviceData device = null;
        Cursor c = null;
        try {
            c = mDB.query(true, table,
                    new String[]{
                            COLUMN.PROFILE_ID,
                            COLUMN.LAST_DAILY_SYNC,
                            COLUMN.LAST_HOURLY_SYNC,
                            COLUMN.TIMEZONE,
                            COLUMN.NAME,
                            COLUMN.MAC,
                    },
                    COLUMN.MAC+"=\""+strAddress+"\"", null, null, null, null, null);
            int iCount = c.getCount();

            if (iCount==0) {
                UtilDBG.i("the address " + strAddress + " is not in the device list");
            } else {
                if (iCount>1) {
                    UtilDBG.e("!! Assume that there is at most one row !!");
                }
                c.moveToNext();

                device = new DeviceData();
                device.mac = strAddress;
                device.profileId = c.getLong(c.getColumnIndex(COLUMN.PROFILE_ID));
                device.lastDailySyncTime = c.getLong(c.getColumnIndex(COLUMN.LAST_DAILY_SYNC));
                device.lastHourlySyncTime = c.getLong(c.getColumnIndex(COLUMN.LAST_HOURLY_SYNC));
                device.lastTimezoneDiff = c.getInt(c.getColumnIndex(COLUMN.TIMEZONE));
                device.displayName = c.getString(c.getColumnIndex(COLUMN.NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return device;
    }

}
