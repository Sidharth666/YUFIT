package com.maxwell.bodysensor.data;

import com.maxwell.bodysensor.util.UtilCalendar;

/*
    Calculate sleep score for a day
 */
public class DSleepData {
    public DSleepData(UtilCalendar date, int duration, double dScore, int timesWoke) {
        mDate = date;
        mDuration = duration;
        mScore = dScore;
        mTimesWoke = timesWoke;
    }

    public UtilCalendar mDate;
    public int mDuration;
    public double mScore;
    public int mTimesWoke;
}
