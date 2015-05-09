package com.maxwell.bodysensor.data;

import com.maxwell.bodysensor.util.UtilCalendar;

/**
 * Created by ryanhsueh on 15/5/5.
 */
public class SleepLogData {
    public SleepLogData(UtilCalendar date, UtilCalendar startTime, UtilCalendar stopTime) {
        mDate = date;
        mStartTime = startTime;
        mStopTime = stopTime;
    }

    public UtilCalendar mDate;
    public UtilCalendar mStartTime;
    public UtilCalendar mStopTime;
}
