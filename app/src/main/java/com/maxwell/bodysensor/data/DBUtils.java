package com.maxwell.bodysensor.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.group.DBGroup15MinutesRecord;
import com.maxwell.bodysensor.data.group.DBGroupDailyRecord;
import com.maxwell.bodysensor.data.group.DBGroupDevice;
import com.maxwell.bodysensor.data.group.DBGroupHourlyRecord;
import com.maxwell.bodysensor.data.group.DBGroupManager;
import com.maxwell.bodysensor.data.group.DBGroupMemberManager;
import com.maxwell.bodysensor.data.group.DBGroupSleepLog;
import com.maxwell.bodysensor.data.group.DBGroupSleepScore;
import com.maxwell.bodysensor.data.sos.DBSOSRecordManager;
import com.maxwell.bodysensor.data.user.DBUser15MinutesRecord;
import com.maxwell.bodysensor.data.user.DBUserDailyRecord;
import com.maxwell.bodysensor.data.user.DBUserDevice;
import com.maxwell.bodysensor.data.user.DBUserHourlyRecord;
import com.maxwell.bodysensor.data.user.DBUserProfile;
import com.maxwell.bodysensor.data.user.DBUserSleepLog;
import com.maxwell.bodysensor.data.user.DBUserSleepScore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DBUtils {

    public static final int VERSION = 4;

	public static final String DB_NAME = "bs.db";

    public static void deleteDBVer3(SQLiteDatabase db) {
        // delete DB Version 3
        db.execSQL("DROP TABLE DBProfile");
        db.execSQL("DROP TABLE DBDevice");
        db.execSQL("DROP TABLE DBDailyRecord");
        db.execSQL("DROP TABLE DBHourlyRecord");
        db.execSQL("DROP TABLE DBSleepRecord");
        db.execSQL("DROP TABLE DBAlarmRecord");
        db.execSQL("DROP TABLE DBDailySleepScoreRecord");
    }

    public static void createUserDB(SQLiteDatabase db) {
        DBUserProfile.createTable(db);

        DBUserDevice.createTable(db);

        DBUserDailyRecord.createTable(db);
        DBUserHourlyRecord.createTable(db);
        DBUser15MinutesRecord.createTable(db);
        DBUserSleepLog.createTable(db);
        DBUserSleepScore.createTable(db);

        DBSOSRecordManager.createTable(db);
    }

    public static void createGroupDB(SQLiteDatabase db) {
        DBGroupManager.createTable(db);
        DBGroupMemberManager.createTable(db);

        DBGroupDevice.createTable(db);

        DBGroupHourlyRecord.createTable(db);
        DBGroupDailyRecord.createTable(db);
        DBGroup15MinutesRecord.createTable(db);
        DBGroupSleepLog.createTable(db);
        DBGroupSleepScore.createTable(db);
    }

    public static void closeCursor(Cursor c) {
        if (c != null && !c.isClosed()) {
            c.close();
        }
    }

    public static String getCurrentTimeAsDateStringInGMT() {
        return getDateStringInGMT(System.currentTimeMillis());
    }

    public static String getDateStringInGMT(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date(millis));
    }

    public static boolean isCursorUsable(Cursor c) {
        return (c != null && c.getCount() > 0);
    }
}
