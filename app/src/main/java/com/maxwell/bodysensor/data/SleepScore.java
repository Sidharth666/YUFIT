package com.maxwell.bodysensor.data;

import java.util.List;

import com.maxwell.bodysensor.util.UtilCalendar;

/*
    Calculate sleep score for a sleep log
 */
public class SleepScore {
    public SleepScore(UtilCalendar date, UtilCalendar startTime, UtilCalendar stopTime) {
        mDate = date;
        mStartTime = startTime;
        mStopTime = stopTime;
    }

    public UtilCalendar mDate;
    public UtilCalendar mStartTime;
    public UtilCalendar mStopTime;
    public int mDiffMinute;
    public double mSleepScore;
    public int mTimesWoke;
    public List<SleepMoveData> mListSleepMove;
};