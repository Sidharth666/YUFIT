package com.maxwell.bodysensor.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.maxwell.bodysensor.data.sleep.SleepLevel;
import com.maxwell.bodysensor.data.sleep.State;
import com.maxwell.bodysensor.data.sleep.StateStart;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanhsueh on 15/5/6.
 */
public abstract class DBSleepScore {

    protected final int DEEP_SLEEP_MOVE_COUNT = 15;
    protected final int LIGHT_SLEEP_MOVE_COUNT = 75;

    public class COLUMN {
        public static final String _ID = "_id";
        public static final String DEVICE_MAC = "deviceMac";
        public static final String DATE = "date";
        public static final String SCORE = "sleepScore";
        public static final String DURATION = "duration";
        public static final String TIMESWOKE = "timesWoke";
        public static final String LOG_TIME = "log_time";
    }


    protected SQLiteDatabase mDB;
    protected DBProgramData mPD;

    public void updateDSleepData(String table, String strSelection, UtilCalendar date, SleepScore score, String deviceMac) {
        if (!date.isDateFormat()) {
            UtilDBG.e("updateDSleepData, first parameter should be isDateFormat");
        }

        ContentValues cv = new ContentValues();
        cv.put(COLUMN.DEVICE_MAC, deviceMac);
        cv.put(COLUMN.DATE, date.getUnixTime());
        cv.put(COLUMN.DURATION, score.mDiffMinute);
        cv.put(COLUMN.SCORE, score.mSleepScore);
        cv.put(COLUMN.TIMESWOKE, score.mTimesWoke);

        Cursor c = mDB.query(true, table,
                new String[]{COLUMN.DEVICE_MAC,COLUMN.DATE,
                        COLUMN.DURATION,COLUMN.SCORE,COLUMN.TIMESWOKE},
                strSelection, null, null, null, null, null);
        int iCount = c.getCount();
        c.close();

        // insert if not exist, update if exist
        if (iCount==0) {
            // insert
            long l = mDB.insert(table, null, cv);
            if (l == -1)
                UtilDBG.e("ERROR, updateDSleepData");
        } else {
            if (iCount>1) {
                UtilDBG.e("!! Assume that there is at most one row !!");
            }
            // Log time has to be updated on update
            String dateString = DBUtils.getCurrentTimeAsDateStringInGMT();
            cv.put(COLUMN.LOG_TIME, dateString);
            mDB.update(table, cv, strSelection, null);
        }
    }

    protected SleepScore calculateSleepScore(UtilCalendar date, UtilCalendar startTime, UtilCalendar stopTime, List<SleepMoveData> sleepMoves) {
        SleepScore score = new SleepScore(date, startTime, stopTime);
        score.mDiffMinute = stopTime.getDiffSeconds(startTime) / 60;

        // 1. to collect "All" sleep data, determine each section is deep/light/awake.
        for (SleepMoveData data : sleepMoves) {
            if (data.mMove >= LIGHT_SLEEP_MOVE_COUNT) {
                data.mLevel = SleepLevel.AWAKE;
            } else if (data.mMove >= DEEP_SLEEP_MOVE_COUNT && data.mMove < LIGHT_SLEEP_MOVE_COUNT) {
                data.mLevel = SleepLevel.LIGHT;
            } else if (data.mMove >= 0 && data.mMove < DEEP_SLEEP_MOVE_COUNT) {
                data.mLevel = SleepLevel.DEEP;
            } else {
                UtilDBG.e("unexpected value, calculateSleepScore");
                data.mLevel = SleepLevel.DEEP;
            }
        }
        score.mListSleepMove = sleepMoves;

        // 2. how many awake times (continues "awake" section)
        for (int i=0; i< sleepMoves.size(); ++i) {
            if (sleepMoves.get(i).mLevel == SleepLevel.AWAKE) {
                int j=i+1;
                for (; j<sleepMoves.size(); ++j) {
                    if (sleepMoves.get(j).mLevel != SleepLevel.AWAKE) {
                        break;
                    }
                }
                i = j - 1;
                ++score.mTimesWoke;
            }
        }

        /*
         3. calculate daily sleep score
            ------------------------------------------------------
                            Awake       Light Sleep     Deep Sleep
            Start           0.0         0.5             1.0
            Awake           0.0         0.6             0.8
            Light Sleep     0.6         0.5             0.9
            Deep Sleep      0.7         0.8             1.0
            ------------------------------------------------------
        */
        int size = sleepMoves.size();
        State sleepState = new StateStart();
        for (int i=1; i<size; ++i) {
            switch (sleepMoves.get(i).mLevel) {
                case DEEP:
                    sleepState = sleepState.nextDeepSleep();
                    break;
                case LIGHT:
                    sleepState = sleepState.nextLightSleep();
                    break;
                case AWAKE:
                    sleepState = sleepState.nextAwake();
                    break;
                default:
                    UtilDBG.e("not expected, fScore += weight_deep");
                    sleepState = sleepState.nextDeepSleep();
                    break;
            }
        }

        float dailyScore = sleepState.getSleepScore() / (size-1);    // did not use the first
        score.mSleepScore = dailyScore * 100.0; // 100: if all "DeepSleep" sections

        return score;

        // [     input     ]   [   shown on page ]   [                    calculation                    ]
        // startTime endTime   to draw    duration   collect sections, to calculate
        // 11:15     7:15      11:15-7:15 480min     (11:30:00-11:44:59) (11:45:00-11:59:59) .. (06:45:00-06:59:59)
        // 11:15     7:14      11:15-7:14 479min     (11:30:00-11:44:59) (11:45:00-11:59:59) .. (06:45:00-06:59:59)
        // 11:15     7:16      11:15-7:16 481min     (11:30:00-11:44:59) (11:45:00-11:59:59) .. (07:00:00-07:14:59)
        // 11:16     7:15      11:16-7:15 479min     (11:30:00-11:44:59) (11:45:00-11:59:59) .. (06:45:00-06:59:59)
        // 11:14     7:15      11:14-7:15 481min     (11:15:00-11:29:59) (11:30:00-11:44:59) .. (06:45:00-06:59:59)
    }

    protected List<DSleepData> queryDSleepData(String table, UtilCalendar begin, UtilCalendar end, String deviceMac) {
        ArrayList<DSleepData> list = new ArrayList<DSleepData>();

        String strSelection = COLUMN.DEVICE_MAC+"=\""+deviceMac+"\"" + " AND " +
                COLUMN.DATE+">=\""+Long.toString(begin.getUnixTime())+"\"" + " AND " +
                COLUMN.DATE+"<=\""+Long.toString(end.getUnixTime())+"\"";

        Cursor c = mDB.query(true, table,
                new String[]{COLUMN.DEVICE_MAC,
                        COLUMN.DATE,
                        COLUMN.DURATION,
                        COLUMN.SCORE,
                        COLUMN.TIMESWOKE},
                strSelection, null, null, null, null, null);
        while (c.moveToNext()) {
            DSleepData d = new DSleepData(
                    new UtilCalendar(c.getLong(c.getColumnIndex(COLUMN.DATE)), null/*UtilTZ.getUTCTZ()*/),
                    c.getInt(c.getColumnIndex(COLUMN.DURATION)),
                    c.getDouble(c.getColumnIndex(COLUMN.SCORE)),
                    c.getInt(c.getColumnIndex(COLUMN.TIMESWOKE)));
            list.add(d);
        }
        c.close();

        return list;
    }
}
