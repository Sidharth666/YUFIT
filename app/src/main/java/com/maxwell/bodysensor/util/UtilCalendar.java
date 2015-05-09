package com.maxwell.bodysensor.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

// note that, get(MONTH) = 0-based value
//                  January = 0 ..
//                  July = 6;
// note that, get(DAY_OF_WEEK)
//                  SUNDAY = 1
//                  MONDAY = 2
//                  TUESDAY = 3
//                  WEDNESDAY = 4
//                  THURSDAY = 5
//                  FRIDAY = 6
//                  SATURDAY = 7

// When implementing a new method, remember to handle following cases
// TimeZone, UTC          + 0:00
//           TPE          + 8:00
//           Nepal        + 5:45
//           Adelaide, AU + 9:30 or + 10:30 (Daylight saving)

public class UtilCalendar extends GregorianCalendar {
    private static final long serialVersionUID = -5729685172858476872L;
    private final boolean mFirstSunday = false;
    // if mFirstSunday = true, the first DAY_OF_WEEK is Sunday
    // if mFirstMonday = false, the first DAY_OF_WEEK is Monday

    public UtilCalendar(Date date, TimeZone tz) {
        if (tz==null) {
            setTimeZone(UtilTZ.getDefaultTZ());
        } else {
            setTimeZone(tz);
        }

        setTime(date);
        set(Calendar.MILLISECOND, 0);
    }

    public UtilCalendar(byte [] value, TimeZone tz) {
        byte bY = 0x00;
        byte bM = 0x00;
        byte bD = 0x00;

        if (tz==null) {
            setTimeZone(UtilTZ.getDefaultTZ());
        } else {
            setTimeZone(tz);
        }

        if (value!=null) {
            if (value.length>=2) {
                bY = (byte)((byte)(value[0]>>1) & (byte)0x7F);
                bM = (byte)((byte)(value[0] & (byte)0x01)<<3);
                bD = (byte)((byte)(value[1]>>5) & (byte)0x07); // temp
                bM |= bD;
                bD = (byte)(value[1] & (byte)0x1F);
            }
            if (value.length==2) {
                // 15-9,Year  8-5,month  4-0,day
                set((bY & 0x000000ff) + 2000, (bM-1) & 0x000000ff, bD & 0x000000ff, 0, 0, 0);
            } else if (value.length==3) {
                // 15-9,Year  8-5,month  4-0,day | hour
                set((bY & 0x000000ff) + 2000, (bM-1) & 0x000000ff, bD & 0x000000ff, value[2] & 0x000000ff, 0, 0);
            } else if (value.length==4) {
                // 15-9,Year  8-5,month  4-0,day | hour | minute
                set((bY & 0x000000ff) + 2000, (bM-1) & 0x000000ff, bD & 0x000000ff, value[2] & 0x000000ff, value[3] & 0x000000ff, 0);
            } else {
                UtilDBG.e("ERROR!!  UtilCalendar, unexpected constructor");
                setTime(new Date());
            }
        } else {
            UtilDBG.e("Unexpected, should call UtilCalendar(TimeZone tz);");
            setTime(new Date());
        }

        set(Calendar.MILLISECOND, 0);

        //           Y       M       D           H          M      S
        // ex.    2014,     02,     26,      05 pm,        28     xx
        // =>       14      02      26          17         28
        // =>  0001110    0010   11010    00010001   00011100           ( bits: 7 + 4 + 5 + 8 + 8 )
        // =>  00011100    01011010       00010001   00011100
        // =>0x   1   c       5   a          1   1      1   c
    }

    public UtilCalendar(int y, int m, int d, int h, int minute, int s, TimeZone tz) {
        if (tz==null) {
            setTimeZone(UtilTZ.getDefaultTZ());
        } else {
            setTimeZone(tz);
        }

        set(y, m-1, d, h, minute, s);
        set(Calendar.MILLISECOND, 0);
    }

    // 1. Unix Time def. (from wiki): the number of seconds that have elapsed since
    // 00:00:00 Coordinated Universal Time (UTC), Thursday, 1 January 1970
    // 2. Verify if the Unix Timestamp is correct or not: http://www.epochconverter.com

    // using UNIX time
    public UtilCalendar(long unixTime, TimeZone tz) {
        if (tz==null) {
            setTimeZone(UtilTZ.getDefaultTZ());
        } else {
            setTimeZone(tz);
        }

        setTimeInMillis(unixTime * 1000);
    }

    public UtilCalendar(TimeZone tz) {
        if (tz==null) {
            setTimeZone(UtilTZ.getDefaultTZ());
        } else {
            setTimeZone(tz);
        }

        setTime(new Date());
    }

    // the value is NOT shifting to UTC timezone
    public byte [] getBSTime(int iSize) {
        if (iSize!=2 && iSize!=3 && iSize!=4 && iSize!=5)
            return null;
        byte [] ret = new byte[iSize];
        byte bY = (byte) (get(YEAR)-2000);
        byte bM = (byte) (get(MONTH)+1);
        byte bD = (byte) (get(DAY_OF_MONTH));
        ret[0] = (byte) (bY<<1 | bM>>3);
        ret[1] = (byte) (bM<<5 | bD);
        if (iSize>=3) {
            ret[2] = (byte) get(HOUR_OF_DAY);
        }
        if (iSize>=4)
            ret[3] = (byte) get(MINUTE);
        if (iSize>=5)
            ret[4] = (byte) get(SECOND);
        return ret;
    }

    // the value is (1) shifting to UTC timezone, and (2) then get the number of seconds that elapsed
    public long getUnixTime() {
        return getTimeInMillis() / 1000;
    }

    public boolean isDateFormat() {
        return ((getUnixTime() % 86400) == 0); // 86400 = 60*60*24
    }

    // return value: positive means "this" is after "compare", negative value means "this" is before "compare"
    public int getDiffSeconds(UtilCalendar compare) {
        return (int)(this.getUnixTime() - compare.getUnixTime());
    }

    public boolean isSameDate(UtilCalendar compare) {
        return (this.get(Calendar.YEAR) == compare.get(Calendar.YEAR) &&
                this.get(Calendar.MONTH) == compare.get(Calendar.MONTH) &&
                this.get(Calendar.DAY_OF_MONTH) == compare.get(Calendar.DAY_OF_MONTH));
    }
    public boolean isSameHM(UtilCalendar compare) {
        return (this.get(Calendar.HOUR_OF_DAY) == compare.get(Calendar.HOUR_OF_DAY) &&
                this.get(Calendar.MINUTE) == compare.get(Calendar.MINUTE));
    }

    // get the first second of the same day/week/month/year
    // get the last second of the same day/week/month/year

    public UtilCalendar getFirstSecondBDay() {
        UtilCalendar first = (UtilCalendar) this.clone();
        first.set(Calendar.HOUR_OF_DAY, 0);
        first.set(Calendar.MINUTE, 0);
        first.set(Calendar.SECOND, 0);
        first.set(Calendar.MILLISECOND, 0);

        return first;
    }

    public UtilCalendar getLastSecondBDay() {
        UtilCalendar last = getFirstSecondBDay();
        last.add(Calendar.DAY_OF_MONTH, 1);
        last.add(Calendar.SECOND, -1);

        return last;
    }

    public UtilCalendar getFirstSecondBWeek() {
        UtilCalendar first = (UtilCalendar) this.clone();
        int iShiftD = 0;
        if (mFirstSunday) {
            first.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        } else {
            if (first.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
                iShiftD -=7;
            }
            first.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }

        first.add(Calendar.DAY_OF_MONTH, iShiftD);
        first.set(Calendar.HOUR_OF_DAY, 0);
        first.set(Calendar.MINUTE, 0);
        first.set(Calendar.SECOND, 0);
        first.set(Calendar.MILLISECOND, 0);

        return first;
    }

    public UtilCalendar getLastSecondBWeek() {
        UtilCalendar last = getFirstSecondBWeek();
        last.add(Calendar.DAY_OF_MONTH, 7);
        last.add(Calendar.SECOND, -1);

        return last;
    }

    public UtilCalendar getFirstSecondBMonth() {
        UtilCalendar first = (UtilCalendar) this.clone();
        first.set(Calendar.DAY_OF_MONTH, 1);
        first.set(Calendar.HOUR_OF_DAY, 0);
        first.set(Calendar.MINUTE, 0);
        first.set(Calendar.SECOND, 0);
        first.set(Calendar.MILLISECOND, 0);

        return first;
    }

    public UtilCalendar getLastSecondBMonth() {
        UtilCalendar last = getFirstSecondBMonth();
        last.add(Calendar.MONTH, 1);
        last.add(Calendar.SECOND, -1);

        return last;
    }

    public UtilCalendar getFirstSecondBYear() {
        UtilCalendar first = (UtilCalendar) this.clone();
        first.set(Calendar.MONTH, Calendar.JANUARY);
        first.set(Calendar.DAY_OF_MONTH, 1);
        first.set(Calendar.HOUR_OF_DAY, 0);
        first.set(Calendar.MINUTE, 0);
        first.set(Calendar.SECOND, 0);
        first.set(Calendar.MILLISECOND, 0);

        return first;
    }

    public UtilCalendar getLastSecondBYear() {
        UtilCalendar last = getFirstSecondBYear();
        last.add(Calendar.YEAR, 1);
        last.add(Calendar.SECOND, -1);

        return last;
    }

//    public UtilCalendar getUTCDate() {
//        return new UtilCalendar(
//                this.get(Calendar.YEAR),
//                this.get(Calendar.MONTH)+1,
//                this.get(Calendar.DAY_OF_MONTH),
//                0, 0, 0, UtilTZ.getUTCTZ());
//    }
//
//    public static int getNumberDays(UtilCalendar first, UtilCalendar last) {
//        if (last.before(first))
//            return -1;
//        int iDiffSecond = last.getDiffSeconds(first);
//        return ((iDiffSecond / 86400) + 1);
//    }
}
