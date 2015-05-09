package com.maxwell.bodysensor.sim;

import java.util.Calendar;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.SharedPrefWrapper;

public class PhNWrapper {
    private static PhNWrapper mInstance;
    private static Context mContext;
    private static MGActivityTrackerApi mMaxwellBLE;
    private static SharedPrefWrapper mSharedPref;
    private static contentObserverCall mObserverCall;
    private static contentObserverSMS mObserverSMS;
    private static contentObserverSMS2 mObserverSMS2;
    private static int mMissedCall = 0;
    private static int mMissedSMS = 0;

    private PhNWrapper() {
        // default constructor hidden, because this is a singleton
    }
    public static void initInstance(Context context) {
        UtilDBG.logMethod();

        if (mInstance==null) {
            mInstance = new PhNWrapper();
        }

        mContext = context;
        mMaxwellBLE = MGActivityTracker.getInstance(mContext);
        mSharedPref = SharedPrefWrapper.getInstance();

        PhNPhoneStateListener phoneListener = new PhNPhoneStateListener(mContext);
        phoneListener.setContentResolver(context.getContentResolver());
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        mObserverCall = new contentObserverCall(new Handler());
        context.getContentResolver().registerContentObserver(Uri.parse("content://call_log/calls"), true, mObserverCall);

        mObserverSMS = new contentObserverSMS(new Handler());
        context.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mObserverSMS);

        mObserverSMS2 = new contentObserverSMS2(new Handler());
        context.getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/inbox"), true, mObserverSMS2);

        getInstance().getMissedCallAndSMSCount(true);
    }

    public static PhNWrapper getInstance() {
        return mInstance;
    }

    public void releaseAll() {
        mContext.getContentResolver().unregisterContentObserver(mObserverCall);
        mContext.getContentResolver().unregisterContentObserver(mObserverSMS);
        mContext.getContentResolver().unregisterContentObserver(mObserverSMS2);
    }
    static class contentObserverCall extends ContentObserver {

        public contentObserverCall(Handler handler) {
            super(handler);
            // you can use a handler if you want or directly do everything onChange();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // Do your stuff here
            super.onChange(selfChange);
            UtilDBG.logMethod();
            // UtilDBG.i("selfChange: " + Boolean.toString(selfChange) + " | uri: " + uri.toString());

            PhNWrapper w = getInstance();
            if (w!=null) {
                w.getMissedCallAndSMSCount(false);
            }
        }
    }

    static class contentObserverSMS extends ContentObserver {

        public contentObserverSMS(Handler handler) {
            super(handler);
            // you can use a handler if you want or directly do everything onChange();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // Do your stuff here
            super.onChange(selfChange);
            UtilDBG.logMethod();
            // UtilDBG.i("selfChange: " + Boolean.toString(selfChange) + " | uri: " + uri.toString());
            PhNWrapper w = getInstance();
            if (w!=null) {
                w.getMissedCallAndSMSCount(false);
            }
        }
    }

    static class contentObserverSMS2 extends ContentObserver {

        public contentObserverSMS2(Handler handler) {
            super(handler);
            // you can use a handler if you want or directly do everything onChange();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // Do your stuff here
            super.onChange(selfChange);
            UtilDBG.logMethod();
            // UtilDBG.i("selfChange: " + Boolean.toString(selfChange) + " | uri: " + uri.toString());
            PhNWrapper w = getInstance();
            if (w!=null) {
                w.getMissedCallAndSMSCount(false);
            }
        }
    }

    public void getMissedCallAndSMSCount(boolean bInit)
    {
        /*
        String[] projection = { CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_LABEL, CallLog.Calls.TYPE };
        String where = CallLog.Calls.TYPE+"="+CallLog.Calls.MISSED_TYPE;
        Cursor c = this.getContentResolver().query(CallLog.Calls.CONTENT_URI,projection,where, null, null);
        c.moveToFirst();
        int iCall = c.getCount();
*/

        final String[] projection = null;
        final String selection = null;
        final String[] selectionArgs = null;
        final String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";
        int iCall = 0;
        Cursor cursorCall = mContext.getContentResolver().query(
                Uri.parse("content://call_log/calls"),
                projection,
                selection,
                selectionArgs,
                sortOrder);

        if (cursorCall != null) {
            while (cursorCall.moveToNext()) {
//            String callLogID = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls._ID));
//            String callNumber = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
//            String callDate = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
                String callType = cursorCall.getString(
                        cursorCall.getColumnIndex(android.provider.CallLog.Calls.TYPE));
                String isCallNew = cursorCall.getString(
                        cursorCall.getColumnIndex(android.provider.CallLog.Calls.NEW));

                if (callType == null || isCallNew == null) {
                    continue;
                }
                if (callType.equals("") || isCallNew.equals("")) {
                    continue;
                }
                if(Integer.parseInt(callType) == android.provider.CallLog.Calls.MISSED_TYPE
                        && Integer.parseInt(isCallNew) > 0){
                    ++ iCall;
                }

            }
            cursorCall.close();
        }

        final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
        Cursor inboxCursor = mContext.getContentResolver().query(
                SMS_INBOX_URI,
                null,
                "read = 0",
                null,
                null);

        int iSMS = 0;
        if (inboxCursor != null) {
            iSMS = inboxCursor.getCount();
            inboxCursor.close();
        }

        UtilDBG.i("getMissedCallAndSMSCount 000 > Missed call: " + iCall + " | SMS: " + iSMS);

        if (bInit) {
            mMissedCall = iCall;
            mMissedSMS = iSMS;
        } else {
            boolean needNotify = needNotifyDevice();

            UtilDBG.i("getMissedCallAndSMSCount 222 > Missed call: " + mMissedCall + " | SMS: " + mMissedSMS);

            if (mMissedCall != iCall) {
                boolean vibrationEnable = false;
                if (iCall > mMissedCall) {
                    vibrationEnable = true;
                }

                mMissedCall = iCall;
                if (needNotify) {
                    UtilDBG.e("Missed call notify : " + iCall);
                    mMaxwellBLE.setMissingCallNumber(iCall, vibrationEnable);
                }
            }

            if (mMissedSMS != iSMS) {
                boolean vibrationEnable = false;
                if (iSMS > mMissedSMS) {
                    vibrationEnable = true;
                }

                mMissedSMS = iSMS;
                if (needNotify) {
                    UtilDBG.e("Missed sms notify : " + iSMS);
                    mMaxwellBLE.setUnreadMessageNumber(iSMS, vibrationEnable);
                }
            }
        }

        /*
        while (cursor.moveToNext()) {
            String callLogID = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls._ID));
            String callNumber = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
            String callDate = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
            String callType = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE));
            String isCallNew = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NEW));
            if(Integer.parseInt(callType) == MISSED_CALL_TYPE && Integer.parseInt(isCallNew) > 0){
                if (_debug) Log.v("Missed Call Found: " + callNumber);
            }
        }
        */
    }

    private boolean needNotifyDevice() {
        if (!mSharedPref.isInComingCallNoDisturbingEnable()) {
            return true;
        } else {
            int startNoDisturb = mSharedPref.getInComingCallNoDisturbingStart();
            int endNoDisturb = mSharedPref.getInComingCallNoDisturbingEnd();

            // get current time to check if need to notify or not
            UtilCalendar cal = new UtilCalendar(null);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            int currentTime = hour * 60 + minute;

            // if current time is not in no disturbing time, phone notify
            if (startNoDisturb > endNoDisturb) {
                if ( !(currentTime >= startNoDisturb || currentTime <= endNoDisturb) ) {
                    return true;
                }
            } else {
                if ( !(currentTime >= startNoDisturb && currentTime <= endNoDisturb) ) {
                    return true;
                }
            }

            return false;
        }
    }

    public int getCurrentMissedCall() { return mMissedCall; }
    public int getCurrentMissedSMS() { return mMissedSMS; }
}
