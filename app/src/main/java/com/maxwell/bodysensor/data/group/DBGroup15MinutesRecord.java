package com.maxwell.bodysensor.data.group;

import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DB15MinutesRecord;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.SleepMoveData;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilTZ;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by ryanhsueh on 15/5/7.
 */
public class DBGroup15MinutesRecord extends DB15MinutesRecord {

    public static final String TABLE = "DB15MinutesRecord_Group";

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN._ID + " INTEGER PRIMARY KEY" + "," +
                COLUMN.DEVICE_MAC + " TEXT NOT NULL" + "," +
                COLUMN.DATE + " INTEGER" + "," +
                COLUMN.MOVE + " INTEGER" +
                ");");
    }

    private static DBGroup15MinutesRecord sManager = null;

    private DBGroup15MinutesRecord(SQLiteDatabase db, DBProgramData pData) {
        mDB = db;
        mPD = pData;
    }

    // Only be init By DBProgramData
    public static synchronized DBGroup15MinutesRecord getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db, DBProgramData pData) {
        if (sManager == null) {
            sManager = new DBGroup15MinutesRecord(db, pData);
        }
    }

    public void releaseInstance() {
        sManager = null;
    }

    public void save15MinBasedSleepMove(Date date, int move, String deviceMac, int iMinuteOffset, long member_id) {
        GroupMemberData member = mPD.getGroupMember(member_id);

        boolean bBeginIsYesterday = false;

        // init, auto monitor
        int sleepBegin = member.sleepLogBegin;
        int sleepEnd = member.sleepLogEnd;
        int iBeginH = sleepBegin / 60;
        int iBeginM = sleepBegin % 60;
        int iEndH = sleepEnd / 60;
        int iEndM = sleepEnd % 60;

        if (sleepBegin > sleepEnd) {
            bBeginIsYesterday = true;
        }

        TimeZone tz = UtilTZ.getTZWithOffset(iMinuteOffset);

        UtilCalendar calDay = save15MinutesRecord(TABLE, date, move, deviceMac, tz,
                iBeginH, iEndH, bBeginIsYesterday);

        if (calDay != null) {
            UtilCalendar calStart = new UtilCalendar(
                    calDay.get(Calendar.YEAR),
                    calDay.get(Calendar.MONTH)+1,
                    calDay.get(Calendar.DAY_OF_MONTH),
                    iBeginH, iBeginM, 0, tz);
            UtilCalendar calStop = new UtilCalendar(
                    calDay.get(Calendar.YEAR),
                    calDay.get(Calendar.MONTH)+1,
                    calDay.get(Calendar.DAY_OF_MONTH),
                    iEndH, iEndM, 0, tz);

            if (bBeginIsYesterday) {
                calStart.add(Calendar.DAY_OF_MONTH, -1);
            }

            DBGroupSleepLog sleepLogMgr = DBGroupSleepLog.getInstance();
            sleepLogMgr.addSleepLog(calDay, calStart, calStop, deviceMac);
        }
    }

    public List<SleepMoveData> querySleepMoveData(UtilCalendar start, UtilCalendar stop, String deviceMac) {
        return querySleepMoveData(TABLE, start, stop, deviceMac);
    }
}
