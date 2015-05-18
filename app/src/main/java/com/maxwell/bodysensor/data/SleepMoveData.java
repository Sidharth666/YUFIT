package com.maxwell.bodysensor.data;

import com.maxwell.bodysensor.data.sleep.SleepLevel;
import com.maxwell.bodysensor.util.UtilCalendar;

/**
 * Created by ryanhsueh on 15/5/5.
 *
 * 15 minutes based sleep move data
 */
public class SleepMoveData {
    public SleepMoveData(UtilCalendar time, int move) {
        mTime = time;
        mMove = move;
        mLevel = SleepLevel.UNKNOWN;
    }

    public UtilCalendar mTime;
    public int mMove;
    public SleepLevel mLevel;
}
