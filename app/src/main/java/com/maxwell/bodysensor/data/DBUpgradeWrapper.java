package com.maxwell.bodysensor.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

/**
 * Created by ryanhsueh on 15/2/12.
 */
public class DBUpgradeWrapper {

    // fake progress delay
    private static final int DELAY_UPGRADE = 5000;

    private static final String DROP_OLD_DB_PERSON = "DROP TABLE person";
    private static final String DROP_OLD_DB_DEVICE = "DROP TABLE device";
    private static final String DROP_OLD_DB_DAY = "DROP TABLE day";
    private static final String DROP_OLD_DB_RECORD = "DROP TABLE record";
    private static final String DROP_OLD_DB_SLEEP = "DROP TABLE sleep";
    private static final String DROP_OLD_DB_ALARM = "DROP TABLE alarm";
    private static final String DROP_OLD_DB_DSLEEP = "DROP TABLE dsleep";

    // version 1 BD "day" table
    private class OLD_DB_DAY {

        public static final String TABLE = "day";

        public static final String COL_DATE = "date";
        public static final String COL_PEDO = "pedo";
        public static final String COL_ENERGY = "energy";
        public static final String COL_CALORIES = "calories";
        public static final String COL_DISTANCE = "distance";
        public static final String COL_GOAL = "energyGoal";
    }

    // version 1 BD "record" table
    private class OLD_DB_RECORD {

        public static final String TABLE = "record";

        public static final String COL_TIME = "time";
        public static final String COL_PEDO = "pedo";
        public static final String COL_ENERGY = "energy";
        public static final String COL_CALORIES = "calories";
        public static final String COL_DISTANCE = "distance";
    }

    // version 1 BD "sleep" table
    private class OLD_DB_SLEEP {

        public static final String TABLE = "sleep";

        public static final String COL_TIME = "time";
        public static final String COL_MOVE_COUNT = "moveCount";
    }

    // version 1 BD "alarm" table
    private class OLD_DB_ALARM {

        public static final String TABLE = "alarm";

        public static final String COL_DATE = "date";
        public static final String COL_START_TIME = "startTime";
        public static final String COL_STOP_TIME = "stopTime";
    }

    // version 1 BD "dsleep" table (sleep score)
    private class OLD_DB_DSLEEP {

        public static final String TABLE = "dsleep";

        public static final String COL_DATE = "date";
        public static final String COL_DURATION = "duration";
        public static final String COL_SLEEP_SCORE = "sleepScore";
        public static final String COL_TIMES_WOKE = "timesWoke";
    }

    private static DBUpgradeWrapper mInstance = null;

    private DBUpgradeListener mListener;
    public interface DBUpgradeListener {
        public void onDBUpgradeStart();
        public void onDBUpgradeFinish();
    }
    public void setDBUpgradeListener(DBUpgradeListener listener) {
        mListener = listener;
    }

    private DBUpgradeWrapper() {
    }

    public static void initInstance() {
        if (mInstance == null) {
            mInstance = new DBUpgradeWrapper();
        }
    }

    public static DBUpgradeWrapper getInstance() {
        return mInstance;
    }

    public void upgradeDB(final SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (oldVersion == 1 && newVersion == 2) {
//            if (mListener != null) {
//                mListener.onDBUpgradeStart();
//            }
//
//            // Create New DB
//            DBUtils.createDB(db);
//
//            // upgrade old BS DB to New BS
//            Handler handler = new Handler();
//            handler.postDelayed(new DBUpgradeRunnable(db), DELAY_UPGRADE);
//        }
    }

//    class DBUpgradeRunnable implements Runnable {
//
//        final SQLiteDatabase db;
//
//        public DBUpgradeRunnable(SQLiteDatabase db) {
//            this.db = db;
//        }
//
//        private void backupDayRecord() {
//            String[] project = {
//                    OLD_DB_DAY.COL_DATE,
//                    OLD_DB_DAY.COL_PEDO,
//                    OLD_DB_DAY.COL_ENERGY,
//                    OLD_DB_DAY.COL_CALORIES,
//                    OLD_DB_DAY.COL_DISTANCE,
//                    OLD_DB_DAY.COL_GOAL};
//            Cursor cr = db.query(OLD_DB_DAY.TABLE, project,
//                    null, null, null, null, OLD_DB_DAY.COL_DATE + " ASC");
//
//            if (cr.moveToFirst()) {
//                do {
//                    ContentValues cv = new ContentValues();
//                    cv.put(DBUtils.DAILY_RECORD.COL_PROFILE_ID, 1);
//
//                    cv.put(DBUtils.DAILY_RECORD.COL_DATE,
//                            cr.getLong(cr.getColumnIndex(OLD_DB_DAY.COL_DATE)) );
//                    cv.put(DBUtils.DAILY_RECORD.COL_PEDO,
//                            cr.getInt(cr.getColumnIndex(OLD_DB_DAY.COL_PEDO)));
//                    cv.put(DBUtils.DAILY_RECORD.COL_APP_ENERGY,
//                            cr.getFloat(cr.getColumnIndex(OLD_DB_DAY.COL_ENERGY)) );
//                    cv.put(DBUtils.DAILY_RECORD.COL_CALORIES,
//                            cr.getFloat(cr.getColumnIndex(OLD_DB_DAY.COL_CALORIES)) );
//                    cv.put(DBUtils.DAILY_RECORD.COL_DISTANCE,
//                            cr.getFloat(cr.getColumnIndex(OLD_DB_DAY.COL_DISTANCE)) );
//                    cv.put(DBUtils.DAILY_RECORD.COL_GOAL,
//                            cr.getInt(cr.getColumnIndex(OLD_DB_DAY.COL_GOAL)) );
//
//                    db.insert(DBUtils.DAILY_RECORD.TABLE, null, cv);
//                } while (cr.moveToNext());
//            }
//            cr.close();
//            db.execSQL(DROP_OLD_DB_DAY);
//        }
//
//        private void backupHourRecord() {
//            String[] project = {
//                    OLD_DB_RECORD.COL_TIME,
//                    OLD_DB_RECORD.COL_PEDO,
//                    OLD_DB_RECORD.COL_ENERGY,
//                    OLD_DB_RECORD.COL_CALORIES,
//                    OLD_DB_RECORD.COL_DISTANCE};
//            Cursor cr = db.query(OLD_DB_RECORD.TABLE, project,
//                    null, null, null, null, OLD_DB_RECORD.COL_TIME + " ASC");
//
//            if (cr.moveToFirst()) {
//                do {
//                    ContentValues cv = new ContentValues();
//                    cv.put(DBUtils.HOURLY_RECORD.COL_PROFILE_ID, 1);
//
//                    cv.put(DBUtils.HOURLY_RECORD.COL_DATE,
//                            cr.getLong(cr.getColumnIndex(OLD_DB_RECORD.COL_TIME)) );
//                    cv.put(DBUtils.HOURLY_RECORD.COL_PEDO,
//                            cr.getInt(cr.getColumnIndex(OLD_DB_RECORD.COL_PEDO)));
//                    cv.put(DBUtils.HOURLY_RECORD.COL_APP_ENERGY,
//                            cr.getFloat(cr.getColumnIndex(OLD_DB_RECORD.COL_ENERGY)) );
//                    cv.put(DBUtils.HOURLY_RECORD.COL_CALORIES,
//                            cr.getFloat(cr.getColumnIndex(OLD_DB_RECORD.COL_CALORIES)) );
//                    cv.put(DBUtils.HOURLY_RECORD.COL_DISTANCE,
//                            cr.getFloat(cr.getColumnIndex(OLD_DB_RECORD.COL_DISTANCE)) );
//
//                    db.insert(DBUtils.HOURLY_RECORD.TABLE, null, cv);
//                } while (cr.moveToNext());
//            }
//            cr.close();
//            db.execSQL(DROP_OLD_DB_RECORD);
//        }
//
//        private void backupSleepData() {
//            String[] project = {
//                    OLD_DB_SLEEP.COL_TIME,
//                    OLD_DB_SLEEP.COL_MOVE_COUNT};
//            Cursor cr = db.query(OLD_DB_SLEEP.TABLE, project,
//                    null, null, null, null, OLD_DB_SLEEP.COL_TIME + " ASC");
//
//            if (cr.moveToFirst()) {
//                do {
//                    ContentValues cv = new ContentValues();
//                    cv.put(DBUtils.SleepRecord.COL_PROFILE_ID, 1);
//
//                    cv.put(DBUtils.SleepRecord.COL_DATE,
//                            cr.getLong(cr.getColumnIndex(OLD_DB_SLEEP.COL_TIME)) );
//                    cv.put(DBUtils.SleepRecord.COL_MOVE,
//                            cr.getInt(cr.getColumnIndex(OLD_DB_SLEEP.COL_MOVE_COUNT)) );
//
//                    db.insert(DBUtils.SleepRecord.TABLE, null, cv);
//                } while (cr.moveToNext());
//            }
//            cr.close();
//            db.execSQL(DROP_OLD_DB_SLEEP);
//        }
//
//        private void backupAlarmData() {
//            String[] project_alarm = {
//                    OLD_DB_ALARM.COL_DATE,
//                    OLD_DB_ALARM.COL_START_TIME,
//                    OLD_DB_ALARM.COL_STOP_TIME};
//            Cursor cr = db.query(OLD_DB_ALARM.TABLE, project_alarm,
//                    null, null, null, null, OLD_DB_ALARM.COL_DATE + " ASC");
//
//            if (cr.moveToFirst()) {
//                do {
//                    ContentValues cv = new ContentValues();
//                    cv.put(DBUtils.AlarmRecord.COL_PROFILE_ID, 1);
//
//                    cv.put(DBUtils.AlarmRecord.COL_DATE,
//                            cr.getLong(cr.getColumnIndex(OLD_DB_ALARM.COL_DATE)) );
//                    cv.put(DBUtils.AlarmRecord.COL_START_TIME,
//                            cr.getLong(cr.getColumnIndex(OLD_DB_ALARM.COL_START_TIME)) );
//                    cv.put(DBUtils.AlarmRecord.COL_STOP_TIME,
//                            cr.getLong(cr.getColumnIndex(OLD_DB_ALARM.COL_STOP_TIME)) );
//
//                    db.insert(DBUtils.AlarmRecord.TABLE, null, cv);
//                } while (cr.moveToNext());
//            }
//            cr.close();
//            db.execSQL(DROP_OLD_DB_ALARM);
//        }
//
//        private void backupSleepScore() {
//            String[] project_dsleep = {
//                    OLD_DB_DSLEEP.COL_DATE,
//                    OLD_DB_DSLEEP.COL_DURATION,
//                    OLD_DB_DSLEEP.COL_SLEEP_SCORE,
//                    OLD_DB_DSLEEP.COL_TIMES_WOKE};
//            Cursor cr = db.query(OLD_DB_DSLEEP.TABLE, project_dsleep,
//                    null, null, null, null, OLD_DB_DSLEEP.COL_DATE + " ASC");
//
//            if (cr.moveToFirst()) {
//                do {
//                    ContentValues cv = new ContentValues();
//                    cv.put(DBUtils.DAILY_SLEEP_SCORE.COL_PROFILE_ID, 1);
//
//                    cv.put(DBUtils.DAILY_SLEEP_SCORE.COL_DATE,
//                            cr.getLong(cr.getColumnIndex(OLD_DB_DSLEEP.COL_DATE)) );
//                    cv.put(DBUtils.DAILY_SLEEP_SCORE.COL_DURATION,
//                            cr.getInt(cr.getColumnIndex(OLD_DB_DSLEEP.COL_DURATION)) );
//                    cv.put(DBUtils.DAILY_SLEEP_SCORE.COL_SCORE,
//                            cr.getFloat(cr.getColumnIndex(OLD_DB_DSLEEP.COL_SLEEP_SCORE)) );
//                    cv.put(DBUtils.DAILY_SLEEP_SCORE.COL_TIMESWOKE,
//                            cr.getInt(cr.getColumnIndex(OLD_DB_DSLEEP.COL_TIMES_WOKE)) );
//
//
//                    db.insert(DBUtils.DAILY_SLEEP_SCORE.TABLE, null, cv);
//                } while (cr.moveToNext());
//            }
//            cr.close();
//            db.execSQL(DROP_OLD_DB_DSLEEP);
//        }
//
//        @Override
//        public void run() {
//            // delete profile & device table
//            db.execSQL(DROP_OLD_DB_PERSON);
//            db.execSQL(DROP_OLD_DB_DEVICE);
//
//            //++ Daily record
//            backupDayRecord();
//
//            //++ Hourly record
//            backupHourRecord();
//
//            //++ sleep data
//            backupSleepData();
//
//            //++ alarm data
//            backupAlarmData();
//
//            //++ sleep score
//            backupSleepScore();
//
//            if (mListener != null) {
//                mListener.onDBUpgradeFinish();
//            }
//        }
//    };

}
