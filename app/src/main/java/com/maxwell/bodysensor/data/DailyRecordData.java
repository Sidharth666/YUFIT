package com.maxwell.bodysensor.data;

import com.maxwell.bodysensor.util.UtilCalendar;

/**
 * Created by ryanhsueh on 15/5/5.
 *
 * Daily record data
 */
public class DailyRecordData {
    public DailyRecordData(UtilCalendar date, int step, double energy, double calories, double distance, int goal) {
        mDate = date;
        mStep = step;
        mAppEnergy = energy;
        mCalories = calories;
        mDistance = distance;
        mGoal = goal;
    }

    public UtilCalendar mDate;
    public int mStep;
    public double mAppEnergy;
    public double mCalories;
    public double mDistance;
    public int mGoal;
}
