package com.maxwell.bodysensor.data.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.maxwell.bodysensor.data.DBDevice;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DeviceData;
import com.maxwell.bodysensor.data.PrimaryProfileData;
import com.maxwell.bodysensor.util.UtilDBG;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanhsueh on 15/5/6.
 */
public class DBUserProfile  extends DBDevice{

    public static final String TABLE = "DBProfile";

    public class COLUMN {
        public static final String _ID = "_id";
        public static final String GENDER = "gender";
        public static final String BIRTHDAY = "birthday";
        public static final String HEIGHT = "height";
        public static final String WEIGHT = "weight";
        public static final String STRIDE = "stride";
        public static final String NAME = "name";
        public static final String PHOTO = "photo";
        public static final String DAILY_GOAL = "dailyGoal";
        public static final String SLEEP_LOG_BEGIN = "sleepLogBegin";
        public static final String SLEEP_LOG_END = "sleepLogEnd";
        public static final String DEVICE_MAC = "deviceMac";
        public static final String IS_PRIMARY_PROFILE = "isPrimaryProfile";
    }

    public static final String[] PROJECTION = {
            COLUMN._ID,
            COLUMN.NAME,
            COLUMN.GENDER,
            COLUMN.BIRTHDAY,
            COLUMN.HEIGHT,
            COLUMN.WEIGHT,
            COLUMN.STRIDE,
            COLUMN.PHOTO,
            COLUMN.DAILY_GOAL,
            COLUMN.SLEEP_LOG_BEGIN,
            COLUMN.SLEEP_LOG_END,
            COLUMN.DEVICE_MAC,
            COLUMN.IS_PRIMARY_PROFILE,
    };

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN._ID + " INTEGER PRIMARY KEY" + "," +
                COLUMN.NAME + " TEXT" + "," +
                COLUMN.GENDER + " INTEGER" + "," +
                COLUMN.BIRTHDAY + " INTEGER" + "," +
                COLUMN.HEIGHT + " INTEGER" + "," +
                COLUMN.WEIGHT + " INTEGER" + "," +
                COLUMN.STRIDE + " INTEGER" + "," +
                COLUMN.PHOTO + " BLOB" + "," +
                COLUMN.DAILY_GOAL + " INTEGER" + "," +
                COLUMN.SLEEP_LOG_BEGIN + " INTEGER" + "," +
                COLUMN.SLEEP_LOG_END + " INTEGER" + "," +
                COLUMN.DEVICE_MAC + " TEXT" + "," +
                COLUMN.IS_PRIMARY_PROFILE + " INTEGER" +
                ");");
    }



    private static DBUserProfile sManager = null;

    private SQLiteDatabase mDB;
    private PrimaryProfileData mPrimaryProfile;

    private DBUserProfile(SQLiteDatabase db,DBProgramData pData) {
        mDB = db;
        mPD = pData;
        mPrimaryProfile = PrimaryProfileData.getInstace();
    }

    // Only be init By DBProgramData
    public static synchronized DBUserProfile getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db,DBProgramData pData) {
        if (sManager == null) {
            sManager = new DBUserProfile(db,pData);
        }
    }

    public void releaseInstance() {
        // if do not set to null
        // bug: close app, then quickly launch app, it will not init mManger correctly
        sManager = null;
    }

    public PrimaryProfileData getPrimaryProfile() {
        return mPrimaryProfile;
    }

    public void initPrimaryProfile() {
        Cursor c = mDB.query(true, TABLE,
                PROJECTION, null, null, null, null, null, null);
        try {
            if (c.getCount() > 0) {
                c.moveToFirst();

                mPrimaryProfile._Id = c.getInt(c.getColumnIndex(COLUMN._ID));
                mPrimaryProfile.name = c.getString(c.getColumnIndex(COLUMN.NAME));
                mPrimaryProfile.gender = c.getInt(c.getColumnIndex(COLUMN.GENDER));
                mPrimaryProfile.birthday = c.getLong(c.getColumnIndex(COLUMN.BIRTHDAY));
                mPrimaryProfile.height = c.getInt(c.getColumnIndex(COLUMN.HEIGHT));
                mPrimaryProfile.weight = c.getInt(c.getColumnIndex(COLUMN.WEIGHT));
                mPrimaryProfile.stride = c.getInt(c.getColumnIndex(COLUMN.STRIDE));

                mPrimaryProfile.photo = c.getBlob(c.getColumnIndex(COLUMN.PHOTO));

                mPrimaryProfile.dailyGoal = c.getInt(c.getColumnIndex(COLUMN.DAILY_GOAL));

                mPrimaryProfile.sleepLogBegin = c.getInt(c.getColumnIndex(COLUMN.SLEEP_LOG_BEGIN));
                mPrimaryProfile.sleepLogEnd = c.getInt(c.getColumnIndex(COLUMN.SLEEP_LOG_END));
                mPrimaryProfile.targetDeviceMac = c.getString(c.getColumnIndex(COLUMN.DEVICE_MAC));
                mPrimaryProfile.isPrimaryProfile = c.getInt(c.getColumnIndex(COLUMN.IS_PRIMARY_PROFILE));

            }
        } catch (Exception e) {
            UtilDBG.e(e.toString());
        } finally {
            if (c != null)
                c.close();
        }
    }

    public void saveUserProfile() {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN.NAME, mPrimaryProfile.name);
        cv.put(COLUMN.GENDER, mPrimaryProfile.gender);
        cv.put(COLUMN.BIRTHDAY, mPrimaryProfile.birthday);
        cv.put(COLUMN.HEIGHT, mPrimaryProfile.height);
        cv.put(COLUMN.WEIGHT, mPrimaryProfile.weight);
        cv.put(COLUMN.STRIDE, mPrimaryProfile.stride);

        cv.put(COLUMN.PHOTO, mPrimaryProfile.photo);

        cv.put(COLUMN.SLEEP_LOG_BEGIN, mPrimaryProfile.sleepLogBegin);
        cv.put(COLUMN.SLEEP_LOG_END, mPrimaryProfile.sleepLogEnd);

        cv.put(COLUMN.DEVICE_MAC, mPrimaryProfile.targetDeviceMac);

        cv.put(COLUMN.IS_PRIMARY_PROFILE, mPrimaryProfile.isPrimaryProfile);
        cv.put(COLUMN.DAILY_GOAL, mPrimaryProfile.dailyGoal);

        String selection = COLUMN.DEVICE_MAC + "=?";
        String[] values = new String[] {mPrimaryProfile.targetDeviceMac};

        Cursor c = mDB.query(TABLE, null, selection, values, null, null, null);
        boolean isUpdated = false;
        if (c != null) {
            if (c.getCount() > 0) {
                // exists. update
                int resp = mDB.update(TABLE, cv, selection, values);
                Log.e("DBUP", resp + " :update");
                isUpdated = true;
            }
            c.close();
        }
        if (!isUpdated) {
            long resp = mDB.insert(TABLE, null, cv);
            Log.e("DBUP", resp + " :insert");
        }

    }

    public int updateProfileDB(String column, int value) {
        ContentValues cv = new ContentValues();
        cv.put(column, value);
        return mDB.update(TABLE, cv, COLUMN._ID + "=" + mPrimaryProfile._Id, null);
    }
    public int updateProfileDB(String column, String value) {
        ContentValues cv = new ContentValues();
        cv.put(column, value);
        return mDB.update(TABLE, cv, COLUMN._ID + "=" + mPrimaryProfile._Id, null);
    }

    public long getPersonId() {
        return mPrimaryProfile._Id;
    }

    // photo raw data
    public byte[] getPersonPhotoData() {
        return mPrimaryProfile.photo;
    }

    // name
    public String getPersonName() {
        return mPrimaryProfile.name;
    }

    // birthday
    public long getPersonBirthday() {
        return mPrimaryProfile.birthday;
    }

    // gender, 0: male, 1: female
    public int getPersonGender() {
        return mPrimaryProfile.gender;
    }

    // height, cm
    public double getPersonHeight() {
        return mPrimaryProfile.height;
    }

    // height, kg
    public double getPersonWeight() {
        return mPrimaryProfile.weight;
    }

    // stride, cm
    public double getPersonStride() {
        return mPrimaryProfile.stride;
    }

    // goal
    public int getPersonGoal() {
        return mPrimaryProfile.dailyGoal;
    }
    public void setPersonGoal(int goal) {
        mPrimaryProfile.dailyGoal = goal;
        updateProfileDB(COLUMN.DAILY_GOAL, goal);
    }

    // Focused Energy Capsule, use device address as ID
    public String getTargetDeviceMac() {
        return mPrimaryProfile.targetDeviceMac;
    }
    public void setTargetDeviceMac(String address) {
        mPrimaryProfile.targetDeviceMac = address;
        updateProfileDB(COLUMN.DEVICE_MAC, address);
    }

    public int getPersonSMBegin() {
        return mPrimaryProfile.sleepLogBegin;
    }

    public int getPersonSMEnd() {
        return mPrimaryProfile.sleepLogEnd;
    }

    public List<DeviceData> getDeviceList() {
        ArrayList<DeviceData> list = new ArrayList<DeviceData>();

        Cursor c = mDB.query(true, TABLE,
                new String[]{
                        COLUMN.GENDER,
                        COLUMN.BIRTHDAY,
                        COLUMN.HEIGHT,
                        COLUMN.STRIDE,
                        COLUMN.NAME,
                        COLUMN.PHOTO,
                        COLUMN.DAILY_GOAL,
                        COLUMN.SLEEP_LOG_BEGIN,
                        COLUMN.SLEEP_LOG_END,
                        COLUMN.DEVICE_MAC,
                        COLUMN.IS_PRIMARY_PROFILE
                },
                null, null, null, null, null, null);
        while (c.moveToNext()) {
            DeviceData device = new DeviceData();
            device.gender = c.getInt(c.getColumnIndex(COLUMN.GENDER));
            device.birthday = c.getInt(c.getColumnIndex(COLUMN.BIRTHDAY));
            device.height = c.getInt(c.getColumnIndex(COLUMN.HEIGHT));
            device.stride = c.getInt(c.getColumnIndex(COLUMN.STRIDE));
            device.name = c.getString(c.getColumnIndex(COLUMN.NAME));
            device.deviceMac = c.getString(c.getColumnIndex(COLUMN.DEVICE_MAC));
            device.photo = c.getBlob(c.getColumnIndex(COLUMN.PHOTO));
            device.dailygoal = c.getInt(c.getColumnIndex(COLUMN.DAILY_GOAL));
            device.sleeplLogBegin = c.getInt(c.getColumnIndex(COLUMN.SLEEP_LOG_BEGIN));
            device.sleepLogEnd = c.getInt(c.getColumnIndex(COLUMN.SLEEP_LOG_END));
            device.deviceMac = c.getString(c.getColumnIndex(COLUMN.DEVICE_MAC));
            device.isPrimaryProfile = c.getInt(c.getColumnIndex(COLUMN.IS_PRIMARY_PROFILE));
            list.add(device);

        }
        c.close();

        return list;
    }


    public void removeDevice(String strAddress) {
        Cursor c = mDB.query(true, TABLE,
                new String[]{
                        DBUserProfile.COLUMN.DEVICE_MAC,
                },
                DBUserProfile.COLUMN.DEVICE_MAC+"=\""+strAddress+"\"", null, null, null, null, null);
        int iCount = c.getCount();

        if (iCount!=0) {
            if (iCount>1) {
                UtilDBG.e("!! Assume that there is at most one row !!");
            } else {
                c.moveToFirst();
            }
        }

        c.close();

        int iNumDevice = mDB.delete(TABLE, DBUserProfile.COLUMN.DEVICE_MAC+"=\""+strAddress+"\"", null);
        //int iNumDevice = mDB.delete(TABLE,null,null);
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


