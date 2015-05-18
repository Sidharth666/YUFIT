package com.maxwell.bodysensor.util;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// DebugData
// 1. save debug message to DB. (info.bs file)
// 2. it is ONLY directly used by UtilDBG.
// 3. To prevent infinite loop, when there is an error
//     do not use UtilDBG, just use log.e

public class DBDebugData extends SQLiteOpenHelper {
    // DBLog
    private final String DBT_LOG = "log";
    private final String DBT_LOG_LAUNCH = "launch";
    private final String DBT_LOG_ELAPSE = "elapse";
    private final String DBT_LOG_LEVEL = "level";
    private final String DBT_LOG_MESSAGE = "message";

    private static DBDebugData instance = null;
    private SQLiteDatabase mDB;

    public DBDebugData(Context context, String name, CursorFactory factory,
            int version) {
        super(context, name, factory, version);
    }

    public static void initInstance(Context ctx) {
        if (instance==null) {
            instance = new DBDebugData(ctx, "info.db", null, 1);
        }
    }

    public static DBDebugData getInstance() {
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DBT_LOG + " (" +
                "_id INTEGER PRIMARY KEY" + "," +
                DBT_LOG_LAUNCH + " INTEGER" + "," +
                DBT_LOG_ELAPSE + " INTEGER" + "," +
                DBT_LOG_LEVEL +  " INTEGER" + "," +
                DBT_LOG_MESSAGE + " TEXT NOT NULL" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void initAll() {
        mDB = getWritableDatabase();
    }

    public void closeAll() {
        mDB.close();
        mDB = null;
    }

    public class DebugMessage {
        public DebugMessage(long launch, long elapse, int level, String message) {
            mLaunch = launch;
            mElapse = elapse;
            mLevel = level;
            mStrMessage = message;
        }
        long mLaunch;
        long mElapse;
        int mLevel;
        String mStrMessage;
    }

    public void saveLog(long launch, long elapse, int level, String str) {
        ContentValues cv = new ContentValues();
        cv.put(DBT_LOG_LAUNCH, launch);
        cv.put(DBT_LOG_ELAPSE, elapse);
        cv.put(DBT_LOG_LEVEL, level);
        cv.put(DBT_LOG_MESSAGE, str);

        long l = mDB.insert(DBT_LOG, null, cv);
        if (l == -1) {
            Log.e(UtilDBG.LOGTAG, "error in DBDebugData::saveLog()");
        }
    }

    public void clearLaunch(long launch) {
        mDB.delete(DBT_LOG, DBT_LOG_LAUNCH+"=\""+Long.toString(launch)+"\"", null);
    }

    public void clearBeyond(int iLimit) {
        ArrayList<Long> list = new ArrayList<Long>();
        Cursor c = mDB.query(true, DBT_LOG, new String[]{DBT_LOG_LAUNCH}, null, null, null, null, null, null);
        while (c.moveToNext()) {
            list.add(c.getLong(c.getColumnIndex(DBT_LOG_LAUNCH)));
        }
        c.close();

        c = mDB.query(true, DBT_LOG, new String[]{DBT_LOG_LAUNCH, DBT_LOG_ELAPSE, DBT_LOG_LEVEL, DBT_LOG_MESSAGE}, null, null, null, null, null, null);
        int iCount = c.getCount();
        c.close();
        for (Long launch : list) {
            if (iCount<=iLimit)
                break;

            clearLaunch(launch.longValue());

            c = mDB.query(true, DBT_LOG, new String[]{DBT_LOG_LAUNCH, DBT_LOG_ELAPSE, DBT_LOG_LEVEL, DBT_LOG_MESSAGE}, null, null, null, null, null, null);
            iCount = c.getCount();
            c.close();
        }
    }

    ArrayList<DebugMessage> getLogs() {
        ArrayList<DebugMessage> list = new ArrayList<DebugMessage>();

        Cursor c = mDB.query(true, DBT_LOG, new String[]{DBT_LOG_LAUNCH, DBT_LOG_ELAPSE, DBT_LOG_LEVEL, DBT_LOG_MESSAGE}, null, null, null, null, null, null);
        while (c.moveToNext()) {
            DebugMessage msg = new DebugMessage(
                    c.getLong(c.getColumnIndex(DBT_LOG_LAUNCH)),
                    c.getLong(c.getColumnIndex(DBT_LOG_ELAPSE)),
                    c.getInt(c.getColumnIndex(DBT_LOG_LEVEL)),
                    c.getString(c.getColumnIndex(DBT_LOG_MESSAGE)));
            list.add(msg);
        }
        c.close();

        return list;
    }
}
