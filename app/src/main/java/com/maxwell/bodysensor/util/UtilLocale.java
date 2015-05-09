package com.maxwell.bodysensor.util;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.maxwell.bodysensor.R;

import android.content.res.Resources;

public class UtilLocale {
    private static Resources mRes = null;
    private static Locale mApp = null;
    private static Locale mEnglish = null;

    public static void init(Resources res) {
        mRes = res;
        if (mRes != null) {
            String strLanguage = mRes.getString(R.string.locale_language);
            String strCountry = mRes.getString(R.string.locale_country);
            if (strLanguage.length()>0 && strCountry.length()>0) {
                mApp = new Locale(strLanguage, strCountry);
            } else if (strLanguage.length()>0 && strCountry.length() == 0) {
                mApp = new Locale(strLanguage);
            } else {
                // unexpected, check strings.xml, and find out locale_language and locale_country
                mApp = Locale.getDefault();
            }
        }
    }

    public static Locale getDefaultLocale() {
        // Since the user's locale changes dynamically,
        // avoid caching this value.
        // Instead, use this method to look it up for each use
        return Locale.getDefault();
    }

    public static Locale getAppLocale() {
        return mApp;
    }

    public static Locale getEnglishLocale() {
        if (mEnglish==null) {
            mEnglish = new Locale("en", "us");
            if (mEnglish==null || mEnglish.getLanguage().compareToIgnoreCase("en")!=0) {
                mEnglish = new Locale("en");
            }

            if (mEnglish==null) {
                UtilDBG.e("not expected, can not get the English locale");
            }
        }

        return mEnglish;
    }


    public enum DateFmt {
        YMDWeek,
        YMD,
        MD,
        YM,
        HMa,
        YMDHMa,
        WeekHMa,
        HMSa,
        HMS_24,
    }

    public static String dateToString(Date date, DateFmt fmt) {
        if (date == null) {
            return "";
        }

        int id;

        switch (fmt) {
        case YMDWeek:       id = R.string.string_datefmt_ymd_week; break;
        case YMD:           id = R.string.string_datefmt_ymd;   break;
        case MD:            id = R.string.string_datefmt_md;    break;
        case YM:            id = R.string.string_datefmt_ym;    break;
        case HMa:           id = R.string.string_datefmt_hma;       break;
        case YMDHMa:        id = R.string.string_datefmt_ymd_hma;   break;
        case WeekHMa:       id = R.string.string_datefmt_week_hma;  break;
        case HMSa:          id = R.string.string_datefmt_hmsa;  break;
        case HMS_24:        id = R.string.string_datefmt_hms_24;    break;
        default:            id = 0;                                 break;
        }
        if (id == 0) {
            return "";
        }

        SimpleDateFormat df = new SimpleDateFormat(mRes.getString(id), UtilLocale.getAppLocale());

        DateFormatSymbols symbols = new DateFormatSymbols(UtilLocale.getAppLocale());
        symbols.setAmPmStrings(new String[] {
            mRes.getString(R.string.string_time_am),
            mRes.getString(R.string.string_time_pm) });
        df.setDateFormatSymbols(symbols);

        return df.format(date);
    }

    public static String calToString(Calendar cal, DateFmt fmt) {
        if (cal == null) {
            return "";
        }
        return dateToString(cal.getTime(), fmt);
    }

}
