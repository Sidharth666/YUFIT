package com.maxwell.bodysensor.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.os.Build;

// Ref.
// http://trivedihardik.wordpress.com/2011/08/20/how-to-avoid-force-close-error-in-android/
// http://stackoverflow.com/questions/16561692/android-exception-handling-best-practice

public class UtilExceptionHandler implements UncaughtExceptionHandler {
    private final String mDeviceInfo;
    private final String mOSInfo;
    private final String mSoftwareInfo;
    public UtilExceptionHandler(String strID) {
        mDeviceInfo = "[Device]" +
                " | BRAND:" + Build.BRAND +
                " | MANUFACTURER:" + Build.MANUFACTURER +
                " | MODEL:" + Build.MODEL +
                " | BOARD:" + Build.BOARD +
                " | TIME:" + Long.toString(Build.TIME);
        mOSInfo = "[Android]" +
                " | SDK_INT:" + Integer.toString(Build.VERSION.SDK_INT) +
                " | Release:" + Build.VERSION.RELEASE;
        mSoftwareInfo = "[Software] | id:" + strID;
    }

    public String getDeviceInfo() { return mDeviceInfo; }
    public String getOSInfo() { return mOSInfo; }
    public String getSoftwareInfo() { return mSoftwareInfo; }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));

        UtilDBG.e("[Cause of Error] stackTrace + | " + stackTrace.toString() +
                mDeviceInfo + "\n" +
                mOSInfo + "\n" +
                mSoftwareInfo);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
