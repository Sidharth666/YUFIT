package com.maxwell.bodysensor.data.group;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwellguider.bluetooth.activitytracker.GoalType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanhsueh on 15/3/18.
 */
public class DBGroupManager {

    public static final String TABLE = "DBGroup";

    public class Column {
        public static final String GROUP_ID = "group_id";
        public static final String NAME = "name";
        public static final String SCHOOL = "school";
        public static final String CLASS = "class";
        public static final String DAILY_GOAL = "daily_goal";
        public static final String GOAL_TYPE = "goal_type";
    }

    private static final String[] PROJECTION = {
            Column.GROUP_ID,
            Column.NAME,
            Column.SCHOOL,
            Column.CLASS,
            Column.DAILY_GOAL,
            Column.GOAL_TYPE,
    };

    public static void createTable (SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                Column.GROUP_ID + " INTEGER PRIMARY KEY" + "," +
                Column.NAME + " TEXT" + "," +
                Column.SCHOOL + " TEXT" + "," +
                Column.CLASS + " TEXT" + "," +
                Column.DAILY_GOAL + " INTEGER" + "," +
                Column.GOAL_TYPE + " INTEGER" +
                ");");
    }

    public static long addNewGroup(SQLiteDatabase db, GroupData group) {
        ContentValues cv = new ContentValues();
        cv.put(Column.NAME, group.name);
        cv.put(Column.SCHOOL, group.school);
        cv.put(Column.CLASS, group._class);
        cv.put(Column.DAILY_GOAL, group.daily_goal);
        cv.put(Column.GOAL_TYPE, group.goal_type.getValue());

        return db.insert(TABLE, null, cv);
    }

    public static long updateGroup(SQLiteDatabase db, GroupData group) {
        ContentValues cv = new ContentValues();
        cv.put(Column.NAME, group.name);
        cv.put(Column.SCHOOL, group.school);
        cv.put(Column.CLASS, group._class);
        cv.put(Column.DAILY_GOAL, group.daily_goal);
        cv.put(Column.GOAL_TYPE, group.goal_type.getValue());

        StringBuilder sbSelection = new StringBuilder();
        sbSelection.append(Column.GROUP_ID).append("=").append(group.group_Id);

        return db.update(TABLE, cv, sbSelection.toString(), null);
    }

    public static List<GroupData> getListAllGroup(SQLiteDatabase db) {
        ArrayList<GroupData> groupList = new ArrayList<GroupData>();

        Cursor cr = db.query(true, TABLE, PROJECTION, null, null, null, null, null, null, null);
        try {
            if (cr.moveToFirst()) {
                do {
                    GroupData group = new GroupData();
                    group.group_Id = cr.getLong(cr.getColumnIndex(Column.GROUP_ID));
                    group.name = cr.getString(cr.getColumnIndex(Column.NAME));
                    group.school = cr.getString(cr.getColumnIndex(Column.SCHOOL));
                    group._class = cr.getString(cr.getColumnIndex(Column.CLASS));
                    group.daily_goal = cr.getInt(cr.getColumnIndex(Column.DAILY_GOAL));
                    group.goal_type = GoalType.getTypeOfValue(cr.getInt(cr.getColumnIndex(Column.GOAL_TYPE)));

                    groupList.add(group);

                } while (cr.moveToNext());
            }
        } finally {
            if (cr != null) {
                cr.close();
            }
        }

        return groupList;
    }

    public static GroupData getGroupData(SQLiteDatabase db, long group_id) {
        GroupData group = null;

        StringBuilder sbSelection = new StringBuilder();
        sbSelection.append(Column.GROUP_ID).append("=").append(group_id);

        Cursor cr = db.query(true, TABLE, PROJECTION,
                sbSelection.toString(), null, null, null, null, null, null);
        try {
            if (cr.moveToFirst()) {
                group = new GroupData();
                group.group_Id = cr.getLong(cr.getColumnIndex(Column.GROUP_ID));
                group.name = cr.getString(cr.getColumnIndex(Column.NAME));
                group.school = cr.getString(cr.getColumnIndex(Column.SCHOOL));
                group._class = cr.getString(cr.getColumnIndex(Column.CLASS));
                group.daily_goal = cr.getInt(cr.getColumnIndex(Column.DAILY_GOAL));
                group.goal_type = GoalType.getTypeOfValue(cr.getInt(cr.getColumnIndex(Column.GOAL_TYPE)));
            }

        } finally {
            if (cr != null) {
                cr.close();
            }
        }

        return group;
    }

    public static boolean deleteGroup(SQLiteDatabase db, long group_id) {
        StringBuilder sbSelection = new StringBuilder();
        sbSelection.append(Column.GROUP_ID).append("=").append(group_id);

        int num = db.delete(TABLE, sbSelection.toString(), null);
        return num > 0;
    }

}
