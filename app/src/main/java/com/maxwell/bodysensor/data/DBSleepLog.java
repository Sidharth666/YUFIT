package com.maxwell.bodysensor.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilTZ;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanhsueh on 15/5/6.
 */
public abstract class DBSleepLog {

    public class COLUMN {
        public static final String _ID = "_id";
        public static final String DEVICE_MAC = "deviceMac";
        public static final String DATE = "date";        // the data is belonged to which day
        public static final String START_TIME = "startTime";   // the actual start point
        public static final String STOP_TIME = "stopTime";     // the actual stop point
    }

    protected SQLiteDatabase mDB;
    protected DBProgramData mPD;


    protected int addSleepLog(String table, UtilCalendar date, UtilCalendar start, UtilCalendar stop, String deviceMac) {
        if (!date.isDateFormat()) {
            UtilDBG.e("addSleepLogData, first parameter should be isDateFormat");
        }

        ContentValues cv = new ContentValues();
        cv.put(COLUMN.DEVICE_MAC, deviceMac);
        cv.put(COLUMN.DATE, date.getUnixTime());
        cv.put(COLUMN.START_TIME, start.getUnixTime());
        cv.put(COLUMN.STOP_TIME, stop.getUnixTime());

        String strSelection = COLUMN.DEVICE_MAC+"=\""+deviceMac+"\"" + " AND " +
                COLUMN.DATE+"=\""+Long.toString(date.getUnixTime())+"\"" + " AND " +
                COLUMN.START_TIME+"=\""+Long.toString(start.getUnixTime())+"\"" + " AND " +
                COLUMN.STOP_TIME+"=\""+Long.toString(stop.getUnixTime())+"\"";

        Cursor c = mDB.query(true, table,
                new String[]{COLUMN.DEVICE_MAC,
                        COLUMN.DATE,
                        COLUMN.START_TIME,
                        COLUMN.STOP_TIME},
                strSelection, null, null, null, null, null);
        int iCount = c.getCount();
        c.close();

        // [1] insert if not exist, then updateDSleepData();
        // [2] do nothing, if there is an identical alarm data, then updateDSleepData();
        if (iCount==0) {
            // insert
            long l = mDB.insert(table, null, cv);
            if (l == -1) {
                UtilDBG.e("ERROR, addSleepLogData");
            }

            return (int)l;
        } else {
            if (iCount>1) {
                UtilDBG.e("!! Assume that there is at most one row !!");
            }
            UtilDBG.i("this is a duplicated alarm data");
            // do nothing, because all SQLiteDB field are used.
            // mDB.update(DBT_ALARM, cv, strSelection, null);

            return 0;
        }
    }

    protected int querySleepLogSize(String table, UtilCalendar date, String deviceMac) {
        if (!date.isDateFormat()) {
            UtilDBG.e("querySleepLogDataSize, first parameter should be isDateFormat");
        }

        Cursor c = mDB.query(true, table,
                new String[]{COLUMN.DEVICE_MAC,
                        COLUMN.DATE,
                        COLUMN.START_TIME,
                        COLUMN.STOP_TIME},
                COLUMN.DEVICE_MAC+"=\""+deviceMac+"\"" + " AND " +
                        COLUMN.DATE+"=\""+Long.toString(date.getUnixTime())+"\"", null, null, null, null, null);
        int iCount = c.getCount();
        c.close();
        return iCount;
    }

    protected SleepLogData querySleepLog(String table, UtilCalendar date, int iIndex, String deviceMac) {
        List<SleepLogData> SleepLogDatas = getSleepLogList(table, date, deviceMac);
        if (SleepLogDatas==null) {
            return null;
        }

        if (iIndex<0 || iIndex>=SleepLogDatas.size()) {
            UtilDBG.e("querySleepLogData, index, out of range");
            return null;
        }

        return SleepLogDatas.get(iIndex);
    }

    protected void deleteSleepLog(String table, UtilCalendar date, int iIndex, String deviceMac) {
        List<SleepLogData> SleepLogDatas = getSleepLogList(table, date, deviceMac);
        if (SleepLogDatas==null) {
            UtilDBG.e("deleteSleepLogData, try to delete an un-exist data [1]");
            return ;
        }

        if (iIndex<0 || iIndex>=SleepLogDatas.size()) {
            UtilDBG.e("deleteSleepLogData, try to delete an un-exist data [2]");
            return ;
        }

        SleepLogData del = SleepLogDatas.get(iIndex);
        int iNum = mDB.delete(table, COLUMN.DEVICE_MAC+"=\""+deviceMac+"\"" + " AND " +
                COLUMN.DATE+"=\""+Long.toString(del.mDate.getUnixTime())+"\"" + " AND " +
                COLUMN.START_TIME+"=\""+Long.toString(del.mStartTime.getUnixTime())+"\"" + " AND " +
                COLUMN.STOP_TIME+"=\""+Long.toString(del.mStopTime.getUnixTime())+"\"", null);
        if (iNum!=1) {
            UtilDBG.e("delete alarm data");
        }
    }

    protected SleepLogData querySleepLogLongest(String table, UtilCalendar date, String deviceMac) {
        List<SleepLogData> SleepLogDatas = getSleepLogList(table, date, deviceMac);
        if (SleepLogDatas==null) {
            return null;
        }

        int iLongestIndex = -1;
        int iDiffLongest = -1;
        int iDiffMinute = -1;
        for (int i=0; i<SleepLogDatas.size(); ++i) {
            SleepLogData adTemp = SleepLogDatas.get(i);
            iDiffMinute = adTemp.mStopTime.getDiffSeconds(adTemp.mStartTime);
            if (iDiffLongest < iDiffMinute) {
                iDiffLongest = iDiffMinute;
                iLongestIndex = i;
            }
        }
        if (iLongestIndex==-1) {
            UtilDBG.e("alarm data longest");
            iLongestIndex = 0;
        }

        return SleepLogDatas.get(iLongestIndex);
    }

    protected List<SleepLogData> getSleepLogList(String table, UtilCalendar date, String deviceMac) {
        if (!date.isDateFormat()) {
            UtilDBG.e("accessSleepLogData, date, must be date format");
        }

        Cursor c = mDB.query(true, table,
                new String[]{COLUMN.DEVICE_MAC,COLUMN.DATE,
                        COLUMN.START_TIME,
                        COLUMN.STOP_TIME},
                COLUMN.DEVICE_MAC+"=\""+deviceMac+"\"" + " AND " +
                        COLUMN.DATE+"=\""+Long.toString(date.getUnixTime())+"\"",
                null, null, null, COLUMN.STOP_TIME + " ASC", null);

        // there are no SleepLogData
        int iCount = c.getCount();
        if (iCount<=0) {
            return null;
        }

        ArrayList<SleepLogData> listSleepLog = new ArrayList<SleepLogData>();

        while (c.moveToNext()) {
            UtilCalendar queryDate = new UtilCalendar(c.getLong(c.getColumnIndex(COLUMN.DATE)), null/*UtilTZ.getUTCTZ()*/);
            UtilCalendar queryStartTime = new UtilCalendar(c.getLong(c.getColumnIndex(COLUMN.START_TIME)), UtilTZ.getDefaultTZ());
            UtilCalendar queryStopTime = new UtilCalendar(c.getLong(c.getColumnIndex(COLUMN.STOP_TIME)), UtilTZ.getDefaultTZ());
            SleepLogData data = new SleepLogData(queryDate, queryStartTime, queryStopTime);
            listSleepLog.add(data);
        }
        c.close();

        return listSleepLog;
    }
}
