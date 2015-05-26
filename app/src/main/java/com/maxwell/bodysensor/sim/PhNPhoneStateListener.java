package com.maxwell.bodysensor.sim;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

//import com.mxw.ble.BleWrapper;
import com.maxwell.bodysensor.SharedPrefWrapper;
import com.maxwell.bodysensor.util.UtilConst;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;
import com.maxwell.bodysensor.util.UtilDBG;

import java.util.Calendar;

// Ref. http://stackoverflow.com/questions/1853220/retrieve-incoming-calls-phone-number-in-android

public class PhNPhoneStateListener extends PhoneStateListener {

    private final int MAX_LENGTH_PHONE_NUMBER_TO_BLE = 11;

    private boolean isPhoneSpeaking = false;
    private boolean isPhoneIncoming = false;
    private ContentResolver mCR;
    private MGActivityTrackerApi mMaxwellBLE;
    private SharedPrefWrapper mSharedPref;

    public PhNPhoneStateListener(Context context) {
        mMaxwellBLE = MGActivityTracker.getInstance(context);
        mSharedPref = SharedPrefWrapper.getInstance();
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        UtilDBG.logMethod();

        if (TelephonyManager.CALL_STATE_RINGING == state) {
            UtilDBG.i("CALL_STATE_RINGING, someone call this number | " + incomingNumber);

            // Incoming call to BLE device
            int lenPhoneNumber = incomingNumber.length();
            if (lenPhoneNumber > MAX_LENGTH_PHONE_NUMBER_TO_BLE) {
                // Only take the last 11 digits for China's telephone system
                incomingNumber = incomingNumber.substring(
                        lenPhoneNumber - MAX_LENGTH_PHONE_NUMBER_TO_BLE, lenPhoneNumber);
            }

            if (mSharedPref.isDeviceIncomingCallEnable()) {
                if (mMaxwellBLE.isReady()) {
                    if (!UtilConst.isInComingCallNoDisturbing()) {
                        mMaxwellBLE.phoneNotification();
                    }
                }
            }

            isPhoneIncoming = true;
        }

        if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
            // active
            UtilDBG.i("CALL_STATE_OFFHOOK, is picking up the phone");

            if (isPhoneIncoming) {
                // mBleWrapper.incomingCallStop();
                isPhoneIncoming = false;
            }

            isPhoneSpeaking = true;
        }

        if (TelephonyManager.CALL_STATE_IDLE == state) {
            // run when class initial and phone call ended, need detect flag
            // from CALL_STATE_OFFHOOK
            UtilDBG.i("CALL_STATE_IDLE, no ringing and no phone speaking");

            if (isPhoneIncoming) {
                // mBleWrapper.incomingCallStop();
                isPhoneIncoming = false;
            }

            if (isPhoneSpeaking) {

                Handler handler = new Handler();

                //Put in delay because call log is not updated immediately when state changed
                // The dialler takes a little bit of time to write to it 500ms seems to be enough
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // get start of cursor
                        UtilDBG.i("Getting Log activity...");
                        String[] projection = new String[]{Calls.NUMBER};
                        Cursor cur = mCR.query(Calls.CONTENT_URI, projection, null, null, Calls.DATE +" desc");
                        if (cur != null && cur.getCount()>0) {
                            cur.moveToFirst();
                            String lastCallnumber = cur.getString(0);
                            UtilDBG.i("lastCallnumber : " + lastCallnumber);

                            cur.close();
                        }

                    }
                },500);

                isPhoneSpeaking = false;
            }

        }
    }

    public void setContentResolver(ContentResolver cr) {
        mCR = cr;
    }
}
