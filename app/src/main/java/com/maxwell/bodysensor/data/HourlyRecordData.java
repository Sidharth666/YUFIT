package com.maxwell.bodysensor.data;

import com.maxwell.bodysensor.util.UtilCalendar;

/**
 * Created by ryanhsueh on 15/5/5.
 *
 * Hourly record data
 */
public class HourlyRecordData {
    public HourlyRecordData(UtilCalendar time, int step, double appEnergy, double dCalories, double distance) {
        mTime = time;
        mStep = step;
        mAppEnergy = appEnergy;
        mCalories = dCalories;
        mDistance = distance;
    }

    public UtilCalendar mTime;
    public int mStep;
    public double mAppEnergy;
    public double mCalories;
    public double mDistance;
}
