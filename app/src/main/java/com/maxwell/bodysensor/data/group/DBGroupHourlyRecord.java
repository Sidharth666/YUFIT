package com.maxwell.bodysensor.data.group;

import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DBHourlyRecord;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.HourlyRecordData;
import com.maxwell.bodysensor.util.UtilCalendar;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ryanhsueh on 15/5/5.
 */
public class DBGroupHourlyRecord extends DBHourlyRecord {

    public static final String TABLE = "DBHourlyRecord_Group";

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN._ID + " INTEGER PRIMARY KEY" + "," +
                COLUMN.DEVICE_MAC + " TEXT NOT NULL" + "," +
                COLUMN.DATE + " INTEGER" + "," +
                COLUMN.STEP + " INTEGER" + "," +
                COLUMN.APP_ENERGY + " REAL" + "," +
                COLUMN.CALORIES + " REAL" + "," +
                COLUMN.DISTANCE + " REAL" +
                ");");
    }

    private static DBGroupHourlyRecord mManager = null;

    private DBGroupHourlyRecord(SQLiteDatabase db, DBProgramData pData) {
        mDB = db;
        mPD = pData;
    }

    // Only be init By DBProgramData
    public static synchronized DBGroupHourlyRecord getInstance() {
        return mManager;
    }

    public static void init(SQLiteDatabase db, DBProgramData pData) {
        if (mManager == null) {
            mManager = new DBGroupHourlyRecord(db, pData);
        }
    }

    public void releaseInstance() {
        mManager = null;
    }

    public void saveHourlyRecord(Date date, int energy, int step, String deviceMac, int iMinuteOffset, long member_id) {
        final GroupMemberData member = mPD.getGroupMember(member_id);

        saveHourlyRecord(TABLE, date, energy, step, deviceMac, iMinuteOffset, member.stride, member.weight);
    }

    public ArrayList<HourlyRecordData> queryHourlyData(UtilCalendar begin, UtilCalendar end, String deviceMac) {
        return queryHourlyRecord(TABLE, begin, end, deviceMac);
    }

    public boolean deleteHourlyRecords(String address) {
        return deleteRecords(TABLE, address);
    }
}
