package com.maxwell.bodysensor.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ryanhsueh on 15/5/8.
 */
public class UtilDate {

    private static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Date getUTCdatetimeAsDate()
    {
        //note: doesn't check for null
        return stringDateToDate(getUTCdatetimeAsString());
    }

    public static String getUTCdatetimeAsString()
    {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());

        return utcTime;
    }

    public static Date stringDateToDate(String StrDate)
    {
        Date dateToReturn = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);

        try
        {
            dateToReturn = (Date)dateFormat.parse(StrDate);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        return dateToReturn;
    }
}
