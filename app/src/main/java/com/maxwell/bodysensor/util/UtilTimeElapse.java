package com.maxwell.bodysensor.util;

public class UtilTimeElapse {
    long mLastTime;
    String mStrID;

    public UtilTimeElapse(String strID) {
        mLastTime = System.nanoTime();
        mStrID = strID;
    }

    public void reset() {
        mLastTime = System.nanoTime();
    }

    public long check(boolean bPrint, boolean bUpdateLastTime) {
        long temp = System.nanoTime();
        long elapse = temp-mLastTime;
        if (bPrint) {
            UtilDBG.i(String.format("%s | %d nanoseconds", mStrID, elapse));
        }
        if (bUpdateLastTime) {
            mLastTime = temp;
        }
        return elapse;
    }

    public static long getSystemNanoTime() {
        return System.nanoTime();
    }
}
