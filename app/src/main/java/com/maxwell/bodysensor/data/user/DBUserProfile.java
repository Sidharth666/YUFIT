package com.maxwell.bodysensor.data.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.PrimaryProfileData;
import com.maxwell.bodysensor.util.UtilConst;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by ryanhsueh on 15/5/6.
 */
public class DBUserProfile {

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

    private DBUserProfile(SQLiteDatabase db) {
        mDB = db;
        mPrimaryProfile = PrimaryProfileData.getInstace();
    }

    // Only be init By DBProgramData
    public static synchronized DBUserProfile getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db) {
        if (sManager == null) {
            sManager = new DBUserProfile(db);
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

        if (mPrimaryProfile._Id == UtilConst.INVALID_INT) {
            mPrimaryProfile._Id = mDB.insert(TABLE, null, cv);
        } else {
            mDB.update(TABLE, cv, COLUMN._ID + "=" + mPrimaryProfile._Id, null);
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

}
