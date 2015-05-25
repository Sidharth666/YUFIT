package com.maxwell.bodysensor.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ryanhsueh on 15/5/5.
 */
public abstract class DBDailyRecord {

    public class COLUMN {
        public static final String _ID = "_id";
        public static final String DEVICE_MAC = "deviceMac";
        public static final String CALORIES = "calories";
        public static final String DATE = "date";
        public static final String DISTANCE = "distance"; // kilometers
        public static final String APP_ENERGY = "energy"; // AppEnergy
        public static final String STEP = "step";
        public static final String GOAL = "energyGoal";
        public static final String LOG_TIME = "log_time";
    }

    protected SQLiteDatabase mDB;
    protected DBProgramData mPD;

    protected void saveDailyRecord(String table, Date date, int energy, int step, String deviceMac,
                                   double dStride, double dWeight, int iGoal) {

        UtilCalendar cal = new UtilCalendar(date, null/*UtilTZ.getUTCTZ()*/);
        double dCalories = (step) * dStride / 100.0 * dWeight * MainActivity.CALORIES_PER_KM_PER_KG / 1000.0;
        double dDistance = step * dStride / 100.0 / 1000.0;

        if (!cal.isDateFormat()) {
            UtilDBG.e("it is expected to be a date format");
        }

        ContentValues cv = new ContentValues();
        cv.put(COLUMN.DEVICE_MAC, deviceMac);
        cv.put(COLUMN.DATE, cal.getUnixTime());
        cv.put(COLUMN.STEP, step);
        cv.put(COLUMN.APP_ENERGY, energy);
        cv.put(COLUMN.CALORIES, dCalories);
        cv.put(COLUMN.DISTANCE, dDistance);
        cv.put(COLUMN.GOAL, iGoal);

        String strSelection = COLUMN.DEVICE_MAC+"=\""+deviceMac+"\"" + " AND " +
                COLUMN.DATE+"=\""+Long.toString(cal.getUnixTime())+"\"";

        Cursor c = mDB.query(true, table,
                new String[]{COLUMN.DEVICE_MAC},
                strSelection, null, null, null, null, null);
        int iCount = c.getCount();
        c.close();

        // insert if not exist, update if exist
        if (iCount==0) {
            // insert
            long l = mDB.insert(table, null, cv);
            if (l == -1)
                UtilDBG.e("ERROR, saveDailyData");
        } else {
            if (iCount>1) {
                UtilDBG.e("!! Assume that there is at most one row !!");
            }
            // Log time has to be updated on update
            String dateString = DBUtils.getCurrentTimeAsDateStringInGMT();
            cv.put(COLUMN.LOG_TIME, dateString);
            mDB.update(table, cv, strSelection, null);
        }
    }

    protected List<DailyRecordData> queryDailyData(String table,
                                           UtilCalendar begin, UtilCalendar end, UtilCalendar today, String deviceMac) {
        ArrayList<DailyRecordData> list = new ArrayList<DailyRecordData>();

        String strSelection = COLUMN.DEVICE_MAC+"=\""+deviceMac+"\"" + " AND " +
                COLUMN.DATE+">="+begin.getUnixTime() + " AND " +
                COLUMN.DATE+"<="+end.getUnixTime();

        Cursor c = mDB.query(true, table,
                new String[]{
                        COLUMN.DEVICE_MAC,
                        COLUMN.DATE,
                        COLUMN.STEP,
                        COLUMN.APP_ENERGY,
                        COLUMN.CALORIES,
                        COLUMN.DISTANCE,
                        COLUMN.GOAL},
                strSelection, null, null, null, null, null);

        while (c.moveToNext()) {
            UtilCalendar date = new UtilCalendar(c.getLong(c.getColumnIndex(COLUMN.DATE)), null);
            if (!date.isDateFormat()) {
                UtilDBG.e("it is expected to be a date format");
            }

            DailyRecordData d = new DailyRecordData(
                    date,
                    c.getInt(c.getColumnIndex(COLUMN.STEP)),
                    c.getDouble(c.getColumnIndex(COLUMN.APP_ENERGY)),
                    c.getDouble(c.getColumnIndex(COLUMN.CALORIES)),
                    c.getDouble(c.getColumnIndex(COLUMN.DISTANCE)),
                    c.getInt(c.getColumnIndex(COLUMN.GOAL))
            );
            list.add(d);
        }
        c.close();

        return list;
    }

    protected DailyRecordData createDailyDataByHourlyRecord(UtilCalendar date, List<HourlyRecordData> dataRecords, int dailyGoal) {
        if (!date.isDateFormat()) {
            UtilDBG.e("getDailyDataByHourlyData, date, isDateFormat");
        }

        if (dataRecords==null || dataRecords.size()<=0) {
            return null;
        }

        int step = 0;
        double energy = 0;
        double calories = 0;
        double distance = 0;
        for (HourlyRecordData dataRecord : dataRecords) {
            step += dataRecord.mStep;
            energy += dataRecord.mAppEnergy;
            calories += dataRecord.mCalories;
            distance += dataRecord.mDistance;
        }

        return (new DailyRecordData(
                date,
                step,
                energy,
                calories,
                distance,
                dailyGoal));
    }

    protected boolean deleteRecords(String table, String address) {
        int num = mDB.delete(table, COLUMN.DEVICE_MAC+"=\"" + address + "\"", null);
        return num > 0;
    }

}
