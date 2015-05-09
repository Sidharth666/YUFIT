package com.maxwell.bodysensor.data.group;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanhsueh on 15/3/19.
 */
public class DBGroupMemberManager {

    public static final String TABLE = "DBGroupMember";

    public class Column {
        public static final String MEMBER_ID = "member_id";
        public static final String GENDER = "gender";
        public static final String BIRTHDAY = "birthday";
        public static final String HEIGHT = "height";
        public static final String WEIGHT = "weight";
        public static final String STRIDE = "stride";
        public static final String NAME = "name";
        public static final String PHOTO = "photo";
        public static final String COL_SLEEP_LOG_BEGIN = "sleepLogBegin";
        public static final String COL_SLEEP_LOG_END = "sleepLogEnd";
        public static final String COL_DEVICE_MAC = "deviceMac";
        public static final String GROUP_ID = "group_id";
        public static final String SCHOOL_ID = "school_id";
        public static final String SEAT_NO = "seat_no";
        public static final String EMAIL = "email";
    }

    public static final String[] PROJECTION = {
            Column.MEMBER_ID,
            Column.NAME,
            Column.GENDER,
            Column.BIRTHDAY,
            Column.HEIGHT,
            Column.WEIGHT,
            Column.STRIDE,
            Column.PHOTO,
            Column.COL_SLEEP_LOG_BEGIN,
            Column.COL_SLEEP_LOG_END,
            Column.COL_DEVICE_MAC,
            Column.GROUP_ID,
            Column.SCHOOL_ID,
            Column.SEAT_NO,
            Column.EMAIL,
    };

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                Column.MEMBER_ID + " INTEGER PRIMARY KEY" + "," +
                Column.NAME + " TEXT" + "," +
                Column.GENDER + " INTEGER" + "," +
                Column.BIRTHDAY + " INTEGER" + "," +
                Column.HEIGHT + " INTEGER" + "," +
                Column.WEIGHT + " INTEGER" + "," +
                Column.STRIDE + " INTEGER" + "," +
                Column.PHOTO + " BLOB" + "," +
                Column.COL_SLEEP_LOG_BEGIN + " INTEGER" + "," +
                Column.COL_SLEEP_LOG_END + " INTEGER" + "," +
                Column.COL_DEVICE_MAC + " TEXT" + "," +
                Column.GROUP_ID + " INTEGER NOT NULL" + "," +
                Column.SCHOOL_ID + " TEXT" + "," +
                Column.SEAT_NO + " INTEGER" + "," +
                Column.EMAIL + " TEXT" +
                ");");
    }

    public static long addNewMember(SQLiteDatabase db, GroupMemberData member) {
        ContentValues cv = new ContentValues();
        cv.put(Column.GENDER, member.gender);
        cv.put(Column.BIRTHDAY, member.birthday);
        cv.put(Column.HEIGHT, member.height);
        cv.put(Column.WEIGHT, member.weight);
        cv.put(Column.STRIDE, member.stride);
        cv.put(Column.NAME, member.name);
        cv.put(Column.PHOTO, member.photo);
        cv.put(Column.COL_SLEEP_LOG_BEGIN, member.sleepLogBegin);
        cv.put(Column.COL_SLEEP_LOG_END, member.sleepLogEnd);
        cv.put(Column.COL_DEVICE_MAC, member.targetDeviceMac);
        cv.put(Column.GROUP_ID, member.group_Id);
        cv.put(Column.SCHOOL_ID, member.school_Id);
        cv.put(Column.SEAT_NO, member.seatNumber);
        cv.put(Column.EMAIL, member.email);

        return db.insert(TABLE, null, cv);
    }

    public static long updateMemberProfile(SQLiteDatabase db, GroupMemberData member) {
        ContentValues cv = new ContentValues();
        cv.put(Column.GENDER, member.gender);
        cv.put(Column.BIRTHDAY, member.birthday);
        cv.put(Column.HEIGHT, member.height);
        cv.put(Column.WEIGHT, member.weight);
        cv.put(Column.STRIDE, member.stride);
        cv.put(Column.NAME, member.name);
        cv.put(Column.PHOTO, member.photo);
        cv.put(Column.COL_SLEEP_LOG_BEGIN, member.sleepLogBegin);
        cv.put(Column.COL_SLEEP_LOG_END, member.sleepLogEnd);
//        cv.put(Column.CURRENT_EC_MAC, member.ADTAddress);
//        cv.put(Column.GROUP_ID, member.group_Id);
        cv.put(Column.SCHOOL_ID, member.school_Id);
        cv.put(Column.SEAT_NO, member.seatNumber);
        cv.put(Column.EMAIL, member.email);

        StringBuilder sbSelection = new StringBuilder();
        sbSelection.append(Column.GROUP_ID).append("=").append(member.group_Id).append(" AND ")
                .append(Column.MEMBER_ID).append("=").append(member.member_Id);

        return db.update(TABLE, cv, sbSelection.toString(), null);
    }

    public static List<GroupMemberData> getListGroupMember(SQLiteDatabase db, long group_id) {
        List<GroupMemberData> memberList = new ArrayList<GroupMemberData>();

        StringBuilder sbSelection = new StringBuilder();
        sbSelection.append(Column.GROUP_ID).append("=").append(group_id);

        Cursor cr = db.query(true, TABLE, PROJECTION,
                sbSelection.toString(), null, null, null, Column.SCHOOL_ID + " ASC", null, null);
        try {
            if (cr.moveToFirst()) {
                do {
                    GroupMemberData member = new GroupMemberData();
                    member.member_Id = cr.getLong(cr.getColumnIndex(Column.MEMBER_ID));
                    member.name = cr.getString(cr.getColumnIndex(Column.NAME));
                    member.gender = cr.getInt(cr.getColumnIndex(Column.GENDER));
                    member.birthday = cr.getLong(cr.getColumnIndex(Column.BIRTHDAY));
                    member.height = cr.getDouble(cr.getColumnIndex(Column.HEIGHT));
                    member.weight = cr.getDouble(cr.getColumnIndex(Column.WEIGHT));
                    member.stride = cr.getDouble(cr.getColumnIndex(Column.STRIDE));
                    member.photo = cr.getBlob(cr.getColumnIndex(Column.PHOTO));
                    member.sleepLogBegin = cr.getInt(cr.getColumnIndex(Column.COL_SLEEP_LOG_BEGIN));
                    member.sleepLogEnd = cr.getInt(cr.getColumnIndex(Column.COL_SLEEP_LOG_END));
                    member.targetDeviceMac = cr.getString(cr.getColumnIndex(Column.COL_DEVICE_MAC));
                    member.group_Id = cr.getLong(cr.getColumnIndex(Column.GROUP_ID));
                    member.school_Id = cr.getString(cr.getColumnIndex(Column.SCHOOL_ID));
                    member.seatNumber = cr.getInt(cr.getColumnIndex(Column.SEAT_NO));
                    member.email = cr.getString(cr.getColumnIndex(Column.EMAIL));

                    memberList.add(member);

                } while (cr.moveToNext());
            }
        } finally {
            if (cr != null) {
                cr.close();
            }
        }

        return memberList;
    }

    public static GroupMemberData getGroupMember(SQLiteDatabase db, long member_id) {
        GroupMemberData member = null;

        StringBuilder sbSelection = new StringBuilder();
        sbSelection.append(Column.MEMBER_ID).append("=").append(member_id);

        Cursor cr = db.query(true, TABLE, PROJECTION,
                sbSelection.toString(), null, null, null, null, null, null);
        try {
            if (cr.moveToFirst()) {
                member = new GroupMemberData();
                member.member_Id = cr.getLong(cr.getColumnIndex(Column.MEMBER_ID));
                member.name = cr.getString(cr.getColumnIndex(Column.NAME));
                member.gender = cr.getInt(cr.getColumnIndex(Column.GENDER));
                member.birthday = cr.getLong(cr.getColumnIndex(Column.BIRTHDAY));
                member.height = cr.getDouble(cr.getColumnIndex(Column.HEIGHT));
                member.weight = cr.getDouble(cr.getColumnIndex(Column.WEIGHT));
                member.stride = cr.getDouble(cr.getColumnIndex(Column.STRIDE));
                member.photo = cr.getBlob(cr.getColumnIndex(Column.PHOTO));
                member.sleepLogBegin = cr.getInt(cr.getColumnIndex(Column.COL_SLEEP_LOG_BEGIN));
                member.sleepLogEnd = cr.getInt(cr.getColumnIndex(Column.COL_SLEEP_LOG_END));
                member.targetDeviceMac = cr.getString(cr.getColumnIndex(Column.COL_DEVICE_MAC));
                member.group_Id = cr.getLong(cr.getColumnIndex(Column.GROUP_ID));
                member.school_Id = cr.getString(cr.getColumnIndex(Column.SCHOOL_ID));
                member.seatNumber = cr.getInt(cr.getColumnIndex(Column.SEAT_NO));
                member.email = cr.getString(cr.getColumnIndex(Column.EMAIL));
            }

        } finally {
            if (cr != null) {
                cr.close();
            }
        }

        return member;
    }

    public static int updateGroupMemberDeviceMac(SQLiteDatabase db, long member_id, String macAddress) {
        ContentValues cv = new ContentValues();
        cv.put(Column.COL_DEVICE_MAC, macAddress);

        StringBuilder sbSelection = new StringBuilder();
        sbSelection.append(Column.MEMBER_ID).append("=").append(member_id);

        return db.update(TABLE, cv, sbSelection.toString(), null);
    }

    public static int deleteGroupMember(SQLiteDatabase db, long member_id) {
        StringBuilder sbSelection = new StringBuilder();
        sbSelection.append(Column.MEMBER_ID).append("=").append(member_id);

        return db.delete(TABLE, sbSelection.toString(), null);
    }

}
