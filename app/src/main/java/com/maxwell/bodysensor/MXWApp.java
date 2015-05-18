package com.maxwell.bodysensor;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.sim.PhNWrapper;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilLocale;
import com.maxwell.bodysensor.util.UtilTimeElapse;
import com.maxwellguider.bluetooth.MGPeripheral;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerListener;

public class MXWApp extends Application implements
        Application.ActivityLifecycleCallbacks,
        MGActivityTrackerListener {

    public static final String ACTION_SMART_KEY_CAMERA = "com.maxwell.action.SMART_KEY_CAMER";
    public static final String ACTION_SMART_KEY_FIND_PHONE = "com.maxwell.action.SMART_KEY_FIND_PHONE";
    public static final String ACTION_SOS = "com.maxwell.action.SOS";
    public static final String ACTION_HME_SYNCPROGRESS= "com.healthifyme.ACTION_YUFIT_SYNC_UPDATE";
    public static final String HME_ACTION = "com.healthifyme.basic.MAIN_ACTIVITY";
    public static final String ACTION_REMOVE_DEVICE = "com.healthifyme.mmx.ACTION_DEVICE_REMOVED";

    public static final int CAMERA_CAPTURE = 10000;
    public static final int GALLERY_PICK = 10001;
    public static final int PIC_CROP = 9999;

	private static final long CLICK_FAST_THRESHOLD = 1000000000L; // nano seconds
    private static final long SOS_FAST_THRESHOLD = 5000000000L; // nano seconds

    private static MGActivityTrackerApi sMaxwellBLE;
    private static SharedPrefWrapper sSharedPref;
    private DBProgramData mPD;

    // phone notification
    private static PhNWrapper sPhNWrapper;

	private static UtilTimeElapse mTooFastElapse;
	private static View mClickFastView = null;

    private String mDevFWRevision = "";
    private String mDevHWRevision = "";
    private String mDevSWRevision = "";
    private String mDevSerialNumber = "";

    private static boolean sIsMainActivityActived = false;
    private static boolean sIsCameraVisible = false;

    @Override
    public void onCreate() {
        super.onCreate();

        initClickFast();

        UtilLocale.init(getResources());

        boolean isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        UtilDBG.init(isDebuggable, getApplicationContext());

        UtilDBG.d("[RYAN] MXWApp >>> onCreate !!");

        SharedPrefWrapper.initInstance(this, "pref_key");
        sSharedPref = SharedPrefWrapper.getInstance();

        DBProgramData.initInstance(this);
        mPD = DBProgramData.getInstance();
        mPD.initAll();

        initMaxwellBleApi();

        registerActivityLifecycleCallbacks(this);

        // Phone Notification
        String permission = "android.permission.READ_PHONE_STATE";
        int res = checkCallingOrSelfPermission(permission);
        boolean bPhN = (res == PackageManager.PERMISSION_GRANTED);
        UtilDBG.i("[RYAN] MXWApp >> phone notify - " + res + " | " + bPhN);
        if (bPhN) {
            PhNWrapper.initInstance(this);
            sPhNWrapper = PhNWrapper.getInstance();
        }

        /*
        // http://stackoverflow.com/questions/4424492/facebook-sdk-for-android-example-app-wont-work
        // facebook, key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.maxwell.bodysensor", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                 MessageDigest md = MessageDigest.getInstance("SHA");
                 md.update(signature.toByteArray());
                 Log.d("Hash Key:", new String(Base64.encode(md.digest(), Base64.DEFAULT)));
            }
         } catch (NameNotFoundException e) {

         } catch (NoSuchAlgorithmException e) {

         }
         */
    }

    public static void cropImage(Activity activity, Uri uri, int width, int height) {
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(uri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", width);
            cropIntent.putExtra("outputY", height);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            activity.startActivityForResult(cropIntent, PIC_CROP);
            return;
        }
        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void initClickFast() {
        mTooFastElapse = new UtilTimeElapse("TOO FAST");
        mClickFastView = null;
    }

    public static boolean isClickFast(View v) {
        if (mClickFastView!=v) {
            mTooFastElapse.check(false, true);
            mClickFastView = v;
            return false;
        }

        if (mTooFastElapse.check(false, false) < CLICK_FAST_THRESHOLD) {
            return true;
        }

        mTooFastElapse.check(false, true);
        // do not need to update view

        return false;
    }

    public static boolean isSOSTooFast() {
        if (mTooFastElapse.check(false, false) < SOS_FAST_THRESHOLD) {
            return true;
        }

        mTooFastElapse.check(false, true);

        return false;
    }

    public void setDevFWRevision(String revision) {
        mDevFWRevision = revision;
    }
    public String getDevFWRevision() {
        return mDevFWRevision;
    }

    public void setDevHWRevision(String revision) {
        mDevHWRevision = revision;
    }
    public String getDevHWRevision() {
        return mDevHWRevision;
    }

    public void setDevSWRevision(String revision) {
        mDevSWRevision = revision;
    }
    public String getDevSWRevision() {
        return mDevSWRevision;
    }

    public void setDevSerialNumber(String serialNumber) {
        mDevSerialNumber = serialNumber;
    }
    public String getDevSerialNumber() {
        return mDevSerialNumber;
    }

    public static boolean isMainActivityActived() {
        return sIsMainActivityActived;
    }

    public static boolean isCameraVisible() {
        return sIsCameraVisible;
    }

    private void initMaxwellBleApi() {
        sMaxwellBLE = MGActivityTracker.getInstance(this);
        sMaxwellBLE.registerActivityTrackerListener(this);
    }

    public static boolean initBleAutoConnection(String address) {
        if (address.equals("")) {
            return false;
        }
        Log.e("MXWApp", address + " :: address");
        boolean enable = sSharedPref.isPhoneConnectionEnable();
        sMaxwellBLE.setAutoConnect(enable);
        return enable;

    }

    public static void connectDevice(final String address) {
        if (!address.equals("")) {
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!sMaxwellBLE.isConnected()) {
                        sMaxwellBLE.connect(address, 5000);
                    }
                }
            }, 3000);
        }
    }

    public static void disableAutoConnection() {
        sMaxwellBLE.disconnect();
        sMaxwellBLE.setAutoConnect(false);
    }

    public static boolean isPowerWatch(String address) {
        if (true) {
            return true;
        }
        MGPeripheral.DeviceType type = sMaxwellBLE.getDeviceType(address);
        return (type == MGPeripheral.DeviceType.POWER_WATCH);
    }

    public static int getTotalMissedCall() {
        if (sPhNWrapper != null) {
            return sPhNWrapper.getCurrentMissedCall();
        }

        return 0;
    }

    public static int getTotalMissedSMS() {
        if (sPhNWrapper != null) {
            return sPhNWrapper.getCurrentMissedSMS();
        }

        return 0;
    }

    @Override
    public void onActivityCreated(Activity a, Bundle b) {
        UtilDBG.d("[RYAN] MXWApp >>> onActivityCreated >> activity : " + a);
        if (a instanceof MainActivity) {
            sIsMainActivityActived = true;
        }
    }

    @Override
    public void onActivityDestroyed(Activity a) {
        UtilDBG.d("[RYAN] MXWApp >>> onActivityDestroyed >> activity : " + a);
        if (a instanceof MainActivity) {
            sIsMainActivityActived = false;

//            if (mPhNWrapper!=null) {
//                mPhNWrapper.releaseAll();
//            }
        }
    }

    @Override
    public void onActivityPaused(Activity a) {
        UtilDBG.d("[RYAN] MXWApp >>> onActivityPaused >> activity : " + a);

        if (a instanceof CameraActivity) {
            sIsCameraVisible = false;
        }
    }

    @Override
    public void onActivityResumed(Activity a) {
        UtilDBG.d("[RYAN] MXWApp >>> onActivityResumed >> activity : " + a);

        if (a instanceof CameraActivity) {
            sIsCameraVisible = true;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity a, Bundle b) {
    }

    @Override
    public void onActivityStarted(Activity a) {
    }

    @Override
    public void onActivityStopped(Activity a) {
    }

    @Override
    public void onSmartKeyTrigger() {
        UtilDBG.i("[RYAN] MXWApp > onSmartKeyTrigger > isCameraVisible : " + isCameraVisible());
        if (isCameraVisible()) {
            Intent intent = new Intent(ACTION_SMART_KEY_CAMERA);
            sendBroadcast(intent);
        } else if (sSharedPref.isFindPhoneEnable()) {
            if (isMainActivityActived()) {
                Intent intent = new Intent(ACTION_SMART_KEY_FIND_PHONE);
                sendBroadcast(intent);
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setAction(ACTION_SMART_KEY_FIND_PHONE);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onSOSTrigger() {
        UtilDBG.i("[RYAN] MXWApp > onSOSTrigger");

        if (MXWApp.isSOSTooFast()) {
            return;
        }

        sMaxwellBLE.stopNotifyingSOS();

        if (isMainActivityActived()) {
            Intent intent = new Intent(ACTION_SOS);
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setAction(ACTION_SOS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onSyncFinish() {
        Log.e("MXWAPP", "onSyncFinish");
        Intent intent = new Intent(ACTION_HME_SYNCPROGRESS);
        intent.putExtra("onSyncFinish", "onSyncFinish");
        sendBroadcast(intent);
    }

    @Override
    public void onSyncFail() {
        Log.e("MXWAPP", "onSyncFail");
        Intent intent = new Intent(ACTION_HME_SYNCPROGRESS);
        intent.putExtra("onSyncFail", "onSyncFail");
        sendBroadcast(intent);
    }

    @Override
    public void onSyncProgressUpdate(int progress, int total) {
        Log.e("MXWAPP", "onSyncProgressUpdate");
        Intent intent = new Intent(ACTION_HME_SYNCPROGRESS);
        intent.putExtra("progress", (int)(progress*100.0/total));
        sendBroadcast(intent);
    }
}
