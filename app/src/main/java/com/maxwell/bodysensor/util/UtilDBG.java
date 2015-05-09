package com.maxwell.bodysensor.util;

import android.content.Context;
import android.util.Log;

// [ANDROID]        [MXW, rule]
// 2 verbose    OFF detail info, ex. print every onCharacteristicRead result
// 3 debug      ON  user event (press a button ..) | Other, ex. function entrance, ..
// 4 info       ON  output debug message for tracing
// 5 WARN       X   ----not used now----
// 6 ERROR      ON  unexpected error
// 7 ASSERT     X   ----not used now----

// [a] mDebuggable = true
// - output the log directly
// - AND, save every log to DB

// [b] mDebuggable = false,
// 1. save every log to DB

// the log in DB
// 1. when MainActivity.onDestroy()
//    - if no error, clear the log created this time
// 2. next time, MainActivity.onCreate, if previous there is more than xxx rows.
//    - clear, until there is less than xxx rows
// 3. press image button in About, log this time as fail
// 4. to output the db data, go check DFAbout

public class UtilDBG {
    public static final String LOGTAG = "MXW";
    private static long mLaunch;
    private static UtilTimeElapse mElapse;
    private static boolean mDebuggable = true;
    private static boolean mNoError = true;
    private static DBDebugData mDD = null;
    private static final int DEBUG_DATA_LIMIT = 10000 ;

    public static void init(boolean debuggable, Context ctx) {
        mLaunch = System.currentTimeMillis();
        mElapse = new UtilTimeElapse("DBG");
        mDebuggable = debuggable;
        mNoError = true;

        DBDebugData.initInstance(ctx);
        mDD = DBDebugData.getInstance();
        mDD.initAll();
        mDD.clearBeyond(DEBUG_DATA_LIMIT);
    }

    public static void close() {
        if (mNoError) {
            mDD.clearLaunch(mLaunch);
        }
    }

    private static long getElapse() {
        return mElapse.check(false, false) / 1000000;
    }

    private static String getElapseString(long l) {
        int iMilli = (int) (l % 1000);
        int iSecond = (int) (l / 1000);
        int iMinute = iSecond / 60;
        iSecond = iSecond % 60;
        int iHour = iMinute /60;
        iMinute = iMinute % 60;
        return String.format("%02d %02d %02d %03d", iHour, iMinute, iSecond, iMilli);
    }

    // some mechanism to turn it on/off dynamically
    public static void v(String str) {
        // Log.v(LOGTAG, " "+ getElapseString() + str);
    }

    public static void d(String str) {
        if (mDebuggable) {
            Log.d(LOGTAG, getElapseString(getElapse()) + " " + str);
        }
        saveDebugDB(Log.DEBUG, str);
    }

    public static void i(String str) {
        if (mDebuggable) {
            Log.i(LOGTAG, getElapseString(getElapse()) + "  " + str);
        }
        saveDebugDB(Log.INFO, str);
    }

    // public static void w(String str) {}

    public static void e(String str) {
        mNoError = false;

        if (mDebuggable) {
            Log.e(LOGTAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Log.e(LOGTAG, getElapseString(getElapse()) + "  " + str);
        }
        saveDebugDB(Log.ERROR, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        saveDebugDB(Log.ERROR, str);
    }

    // public static void a(String str) {}

    // log the class name and method name
    // ref. http://stackoverflow.com/questions/8340785/is-there-any-way-to-know-the-caller-class-name
    // note, it is in a reverse way.
    public static void logMethod() {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        d(String.format("%4d | %24s [%4d] | %s",
                Thread.currentThread().getId(),
                extractSimpleClassName(ste[3].getClassName()),
                ste[3].getLineNumber(),
                ste[3].getMethodName()));
    }

    public static void logStackTrace() {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        d("-------- [logStackTrace] threadId = " + Long.toString(Thread.currentThread().getId()));
        for (int i=ste.length-1; i>2; --i) {
            d(extractSimpleClassName(ste[i].getClassName()) + " [" +
                    ste[i].getLineNumber() + "] | " +
                    ste[i].getMethodName());
        }
        d("++++++++ [logStackTrace]");
    }

    private static String extractSimpleClassName(String fullClassName) {
        if ((null == fullClassName) || ("".equals(fullClassName)))
          return "";

        int lastDot = fullClassName.lastIndexOf('.');
        if (0 > lastDot)
            return fullClassName;

        return fullClassName.substring(++lastDot);
    }

    private static void saveDebugDB(int level, String str) {
        mDD.saveLog(mLaunch, getElapse(), level, str);
    }

    public static boolean isDebuggable() {
        return mDebuggable;
    }
}