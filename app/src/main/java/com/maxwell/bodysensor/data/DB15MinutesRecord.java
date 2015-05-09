package com.maxwell.bodysensor.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by ryanhsueh on 15/5/7.
 */
public class DB15MinutesRecord {

    public class COLUMN {
        public static final String _ID = "_id";
        public static final String DEVICE_MAC = "deviceMac";
        public static final String DATE = "date";
        public static final String MOVE = "moveCount";
    }

    protected SQLiteDatabase mDB;
    protected DBProgramData mPD;



    protected UtilCalendar save15MinutesRecord(String table, Date date, int move, String deviceMac, TimeZone tz,
                                                   int iBeginH, int iEndH, boolean bBeginIsYesterday) {
        /*
        * save sleep move count per 15 min
        * */
        UtilCalendar cal = new UtilCalendar(date, tz);

        ContentValues cv = new ContentValues();
        cv.put(COLUMN.DEVICE_MAC, deviceMac);
        cv.put(COLUMN.DATE, cal.getUnixTime());
        cv.put(COLUMN.MOVE, move);

        String strSelection = COLUMN.DEVICE_MAC+"=\""+deviceMac+"\"" + " AND " +
                COLUMN.DATE+"=\""+Long.toString(cal.getUnixTime()) + "\"";

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
                UtilDBG.e("ERROR, saveSleepData");
        } else {
            if (iCount>1) {
                UtilDBG.e("!! Assume that there is at most one row !!");
            }
            mDB.update(table, cv, strSelection, null);
        }



        /*
        * save sleep start/end time and calculate sleep quality for this day
        * */
        boolean bNeedAdd = false;
        int iH = cal.get(Calendar.HOUR_OF_DAY);
        UtilCalendar calDay = cal.getFirstSecondBDay();

        if (bBeginIsYesterday) {
            if (iH >= iBeginH) {
                bNeedAdd = true;
                calDay.add(Calendar.DAY_OF_MONTH, 1);
            } else if (iH <= iEndH) {
                bNeedAdd = true;
            }
        } else {
            if (iH>=iBeginH && iH<=iEndH) {
                bNeedAdd = true;
            }
        }

        if (bNeedAdd) {
            return calDay;
        }

        return null;
    }

    protected List<SleepMoveData> querySleepMoveData(String table, UtilCalendar start, UtilCalendar stop, String deviceMac) {
        int iStartMinute = start.get(Calendar.MINUTE);
        int iStartShift = -(iStartMinute % 15);
        UtilCalendar calQuery = (UtilCalendar) start.clone();
        calQuery.add(Calendar.MINUTE, iStartShift);

        String strSelection = COLUMN.DEVICE_MAC+"=\""+deviceMac+"\"" + " AND " +
                COLUMN.DATE+">="+calQuery.getUnixTime() + " AND " +
                COLUMN.DATE+"<"+stop.getUnixTime();

        ArrayList<SleepMoveData> list = new ArrayList<SleepMoveData>();
        for (;calQuery.before(stop); calQuery.add(Calendar.MINUTE, 15)) {
            SleepMoveData d = new SleepMoveData(
                    (UtilCalendar) calQuery.clone(),
                    0);
            list.add(d);
        }

        Cursor c = mDB.query(true, table,
                new String[]{COLUMN.DEVICE_MAC,COLUMN.DATE,COLUMN.MOVE},
                strSelection, null, null, null, null, null);

        while (c.moveToNext()) {
            boolean bFound = false;
            for (SleepMoveData d : list) {
                if (d.mTime.getUnixTime() == c.getLong(c.getColumnIndex(COLUMN.DATE))) {
                    if (d.mMove!=0) {
                        UtilDBG.e("unexpected, more than 1 SleepData is added");
                    }
                    d.mMove += c.getInt(c.getColumnIndex(COLUMN.MOVE));
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                UtilDBG.e("a SleepData is not found in the list");
            }
        }
        c.close();

        return list;
    }
}
