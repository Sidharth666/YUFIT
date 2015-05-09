package com.maxwell.bodysensor.data.sos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanhsueh on 15/4/24.
 */
public class DBSOSRecordManager {

    public static final String TABLE = "DBSOSHistory";

    public class COLUMN {
        public static final String _ID = "_id";
        public static final String DATE = "date";
        public static final String CONTACT_NAME = "contactName";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE  = "longitude";

    }

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN._ID + " INTEGER PRIMARY KEY" + "," +
                COLUMN.DATE + " TEXT NOT NULL" + "," +
                COLUMN.CONTACT_NAME + " INTEGER NOT NULL" + "," +
                COLUMN.LATITUDE + " INTEGER" + "," +
                COLUMN.LONGITUDE + " INTEGER" +
                ");");
    }

    private static DBSOSRecordManager sManager = null;

    private SQLiteDatabase mDB;

    private DBSOSRecordManager(SQLiteDatabase db) {
        mDB = db;
    }

    // Only be init By DBProgramData
    public static synchronized DBSOSRecordManager getInstance() {
        return sManager;
    }

    public static void init(SQLiteDatabase db) {
        if (sManager == null) {
            sManager = new DBSOSRecordManager(db);
        }
    }

    public void releaseInstance() {
        sManager = null;
    }

    public void saveSOSRecord(SOSRecord sos) {
        ContentValues cv = new ContentValues();

        cv.put(COLUMN.DATE, sos.dateUnixTime);
        cv.put(COLUMN.CONTACT_NAME, sos.contactName);
        cv.put(COLUMN.LATITUDE, sos.latitude);
        cv.put(COLUMN.LONGITUDE, sos.longitude);

        mDB.insert(TABLE, null, cv);
    }

    public List<SOSRecord> getAllSOSRecords() {
        List<SOSRecord> listSOSRecord = null;

        Cursor c = mDB.query(true, TABLE,
                new String[]{
                        COLUMN.DATE,
                        COLUMN.CONTACT_NAME,
                        COLUMN.LATITUDE,
                        COLUMN.LONGITUDE},
                null, null, null, null, COLUMN.DATE + " DESC", null);

        try {
            listSOSRecord = new ArrayList<SOSRecord>();
            c.moveToFirst();
            do {
                SOSRecord sos = new SOSRecord(
                        c.getLong(c.getColumnIndex(COLUMN.DATE)),
                        c.getString(c.getColumnIndex(COLUMN.CONTACT_NAME)),
                        c.getDouble(c.getColumnIndex(COLUMN.LATITUDE)),
                        c.getDouble(c.getColumnIndex(COLUMN.LONGITUDE))
                );
                listSOSRecord.add(sos);

            } while (c.moveToNext());

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (c != null) c.close();
        }

        return listSOSRecord;
    }

}
