package com.maxwell.bodysensor.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilTZ;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ryanhsueh on 15/5/5.
 */
public abstract class DBHourlyRecord {

    public class COLUMN {
        public static final String _ID = "_id";
        public static final String DEVICE_MAC = "deviceMac";
        public static final String CALORIES = "calories";
        public static final String DATE = "date";
        public static final String DISTANCE = "distance"; // kilometers
        public static final String APP_ENERGY = "energy"; // AppEnergy
        public static final String STEP = "step";
    }

    public static final String[] PROJECTION = {
            COLUMN._ID,
            COLUMN.DEVICE_MAC,
            COLUMN.CALORIES,
            COLUMN.DATE,
            COLUMN.DISTANCE,
            COLUMN.APP_ENERGY,
            COLUMN.STEP,
    };

    protected SQLiteDatabase mDB;
    protected DBProgramData mPD;

    protected void saveHourlyRecord(String table, Date date, int energy, int step,
                                 String deviceMac, int iMinuteOffset, double dStride, double dWeight) {

        TimeZone tz = UtilTZ.getTZWithOffset(iMinuteOffset);
        UtilCalendar cal = new UtilCalendar(date, tz);

        double dCalories = step * dStride / 100.0 * dWeight * MainActivity.CALORIES_PER_KM_PER_KG / 1000.0;
        double dDistance = step * dStride / 100.0 / 1000.0;

        ContentValues cv = new ContentValues();
        cv.put(COLUMN.DEVICE_MAC, deviceMac);
        cv.put(COLUMN.DATE, cal.getUnixTime());
        cv.put(COLUMN.STEP, step);
        cv.put(COLUMN.APP_ENERGY, energy);
        cv.put(COLUMN.CALORIES, dCalories);
        cv.put(COLUMN.DISTANCE, dDistance);

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
                UtilDBG.e("ERROR, saveHourlyData");
        } else {
            if (iCount>1) {
                UtilDBG.e("!! Assume that there is at most one row !!");
            }
            mDB.update(table, cv, strSelection, null);
        }

    }

    protected ArrayList<HourlyRecordData> queryHourlyRecord(String table,
                                                 UtilCalendar begin, UtilCalendar end, String deviceMac) {
        ArrayList<HourlyRecordData> list = new ArrayList<HourlyRecordData>();
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
                        COLUMN.DISTANCE},
                strSelection, null, null, null, null, null);

        while (c.moveToNext()) {
            HourlyRecordData d = new HourlyRecordData(
                    new UtilCalendar(
                            c.getLong(c.getColumnIndex(COLUMN.DATE)),
                            UtilTZ.getDefaultTZ()),
                    c.getInt(c.getColumnIndex(COLUMN.STEP)),
                    c.getDouble(c.getColumnIndex(COLUMN.APP_ENERGY)),
                    c.getDouble(c.getColumnIndex(COLUMN.CALORIES)),
                    c.getDouble(c.getColumnIndex(COLUMN.DISTANCE)));
            list.add(d);
        }
        c.close();

        return list;
    }

    protected boolean deleteRecords(String table, String address) {
        int num = mDB.delete(table, COLUMN.DEVICE_MAC+"=\"" + address + "\"", null);
        return num > 0;
    }
}
