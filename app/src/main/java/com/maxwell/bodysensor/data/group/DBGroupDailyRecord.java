package com.maxwell.bodysensor.data.group;

import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DBDailyRecord;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DailyRecordData;
import com.maxwell.bodysensor.data.HourlyRecordData;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilTZ;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ryanhsueh on 15/5/5.
 */
public class DBGroupDailyRecord extends DBDailyRecord {

    public static final String TABLE = "DBDailyRecord_Group";

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN._ID + " INTEGER PRIMARY KEY" + "," +
                COLUMN.DEVICE_MAC + " TEXT NOT NULL" + "," +
                COLUMN.DATE + " INTEGER" + "," +
                COLUMN.STEP + " INTEGER" + "," +
                COLUMN.APP_ENERGY + " REAL" + "," +
                COLUMN.CALORIES + " REAL" + "," +
                COLUMN.DISTANCE + " REAL" + "," +
                COLUMN.GOAL + " INTEGER" +
                ");");
    }

    private static DBGroupDailyRecord sManager = null;

    private DBGroupDailyRecord(SQLiteDatabase db, DBProgramData pData) {
        mDB = db;
        mPD = pData;
    }

    // Only be init By DBProgramData
    public static synchronized DBGroupDailyRecord getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db, DBProgramData pData) {
        if (sManager == null) {
            sManager = new DBGroupDailyRecord(db, pData);
        }
    }

    public void releaseInstance() {
        sManager = null;
    }

    public void saveDailyRecord(Date date, int energy, int step, String deviceMac, long group_id, long member_id) {
        final GroupData group = mPD.getGroupData(group_id);
        final GroupMemberData member = mPD.getGroupMember(member_id);

        saveDailyRecord(TABLE, date, energy, step, deviceMac, member.stride, member.weight, group.daily_goal);
    }

    public List<DailyRecordData> queryDailyData(UtilCalendar begin, UtilCalendar end, UtilCalendar today,
                                           String deviceMac, int daily_goal) {
        List<DailyRecordData> list = queryDailyData(TABLE, begin, end, today, deviceMac);

        if (today.compareTo(begin)>=0 &&
                today.compareTo(end)<=0) {
            DailyRecordData data = getDailyDataByHourlyRecord(today, deviceMac, daily_goal);
            if (data!=null) {
                list.add(data);
            }
        }

        return list;
    }

    private DailyRecordData getDailyDataByHourlyRecord(UtilCalendar date, String deviceMac, int daily_goal) {
        UtilCalendar localTime = new UtilCalendar(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH)+1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                UtilTZ.getDefaultTZ());

        DBGroupHourlyRecord hourRecord = DBGroupHourlyRecord.getInstance();
        List<HourlyRecordData> dataRecords = hourRecord.queryHourlyData(
                localTime.getFirstSecondBDay(), localTime.getLastSecondBDay(), deviceMac);

        return createDailyDataByHourlyRecord(date, dataRecords, daily_goal);
    }

    public boolean deleteDailyRecords(String address) {
        return deleteRecords(TABLE, address);
    }

}
