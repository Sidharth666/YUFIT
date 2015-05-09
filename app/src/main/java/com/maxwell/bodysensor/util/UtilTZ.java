package com.maxwell.bodysensor.util;

import java.util.TimeZone;

public class UtilTZ {
    public static TimeZone getDefaultTZ() {
        // Since the user's time zone changes dynamically,
        // avoid caching this value.
        // Instead, use this method to look it up for each use.
        return TimeZone.getDefault();
    }

    // the offset of mDefault in minutes
    public static int getDefaultRawOffset() {
        TimeZone tzDefault = getDefaultTZ();
        if (tzDefault!=null) {
            return tzDefault.getRawOffset() / 1000 / 60;
        }

        return 0;
    }

    public static TimeZone getUTCTZ() {
        TimeZone utc;

        String [] list = TimeZone.getAvailableIDs(0);
        final String prefer = "utc";
        int iIndex;
        for (iIndex=0; iIndex<list.length; ++iIndex) {
            if (list[iIndex].compareToIgnoreCase(prefer)==0) {
                break;
            }
        }
        if (iIndex==list.length) {
            UtilDBG.e("did not find the utc TimeZone, set to the first one w/ offset 0");
            iIndex = 0;
        }
        utc = TimeZone.getTimeZone(list[iIndex]);

        if (utc==null) {
            UtilDBG.e("not expected, can not get the UTC time zone");
        }

        return utc;
    }

    public static TimeZone getTZWithOffset(int iMinute) {
        String id = "GMT+00:00";
        if (iMinute==0) {
            id = "GMT+00:00";
        } else {
            int iDiff = 0;
            if (iMinute>0) {
                id = "GMT+";
                iDiff = iMinute;
            } else if (iMinute<0) {
                id = "GMT-";
                iDiff = -iMinute;
            }

            String tail = String.format("%02d:%02d", iDiff/60, iDiff%60);
            id += tail;
        }

        return TimeZone.getTimeZone(id);
    }
}
