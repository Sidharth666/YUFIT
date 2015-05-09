package com.maxwell.bodysensor.util;

import java.util.Calendar;

public class UtilTime {

	public static long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	public static long currentUnixTime() {
		return System.currentTimeMillis() / 1000;
	}

	public static long getUnixTime(long millis) {
		return millis / 1000;
	}

    public static long getMillisecond(long unixTime) {
        return unixTime * 1000;
    }

    public static long getMillisForIntTime(int time) {
        int hour = time / 60;
        int minute = time % 60;
        UtilCalendar cal = new UtilCalendar(null);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);

        return cal.getTimeInMillis();
    }

}
