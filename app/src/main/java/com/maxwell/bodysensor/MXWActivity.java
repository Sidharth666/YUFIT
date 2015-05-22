package com.maxwell.bodysensor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.listener.OnBackPressedListener;
import com.maxwell.bodysensor.listener.OnCropImageCallback;
import com.maxwell.bodysensor.listener.OnPairDeviceListener;
import com.maxwell.bodysensor.listener.OnSyncDeviceListener;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwellguider.bluetooth.AdvertisingData;
import com.maxwellguider.bluetooth.AttributeListener;
import com.maxwellguider.bluetooth.BTEventListener;
import com.maxwellguider.bluetooth.MGPeripheral;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerDBDelegate;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerListener;

/**
 * Created by ryanhsueh on 15/4/29.
 */
public abstract class MXWActivity extends ActionBarActivity implements
        MGActivityTrackerDBDelegate,
        BTEventListener,
        MGActivityTrackerListener,
        AttributeListener {

    // dialogs
    public static final String DLG_MESSAGE_YN = "dlg_message_yn";
    public static final String DLG_MESSAGE_OK = "dlg_message_ok";
    public static final String DLG_I_PROGRESS = "dlg_i_progress";
    public static final String DLG_TIME_PICKER = "dlg_time_picker";
    public static final String DLG_SWITCH_DEVICES = "dlg_switch_devices";
    public static final String DLG_SWITCH_MODE = "dlg_switch_mode";
    public static final String DLG_SINGLE_CHOOSE = "dlg_single_choose";

    // the dialog fragments, pop up from bottom
    public static final String DF_SHARE = "df_share";
    public static final String DF_OPTIONS = "df_options";
    public static final String DF_EDIT_PHOTO = "df_edit_photo";

    // the dialog fragments, setting
    public static final String DF_PROFILE = "df_profile";
    public static final String DF_GOAL_SETTING = "df_goal_setting";
    public static final String DF_SOCIAL_ACCOUNTS = "df_social_accounts";
    public static final String DF_ADD_NEW_DEVICE = "df_add_new_device";
    public static final String DF_DEVICE_LIST = "df_device_list";
    public static final String DF_DEVICE_INFO = "df_device_info";
    public static final String DF_BLANK = "df_blank";

    // the dialog fragments, trends
    public static final String DF_LOG_SLEEP_ENTRY = "df_log_sleep_entry";

    // the dialog fragments, workout
    public static final String DF_WORKOUT = "df_workout";

    // the dialog fragments, for e2max
    public static final String DF_PHONE_CONNECTION = "df_phone_connection";
    public static final String DF_PHONE_NOTIFY = "df_phone_notify";
    public static final String DF_OUT_OF_RANGE_ALERT = "df_out_of_range_alert";
    public static final String DF_DEVICE_ALARM = "df_device_alarm";
    public static final String DF_TASK_ALERT = "df_task_alert";
    public static final String DF_MOVE_ALERT = "df_move_alert";
    public static final String DF_INFO = "df_info";

    // SOS
    public static final String DF_EMERGENCY_CONTACT = "df_emergency_contact";
    public static final String DF_SOS_HISTORY = "df_sos_history";

    // the dialog fragments, about
    public static final String DF_TUTORIAL = "df_tutorial";
    public static final String DF_ABOUT = "df_about";

    // DialogFragment for Group Mode
    public static final String DF_ADD_NEW_GROUP = "df_add_new_group";
    public static final String DF_SETUP_GROUP = "df_setup_group";
    public static final String DF_ADD_GROUP_MEMBER = "df_add_group_member";
    public static final String DF_GROUP_INFO = "df_group_info";

    protected MGActivityTrackerApi mMaxwellBLE;

    protected SharedPrefWrapper mSharedPref;
    protected DBProgramData mPD;

    private boolean mIsSyncing = false;
    private int mSyncProgress = 100;

    protected OnPairDeviceListener mPairDeviceListener;
    protected OnSyncDeviceListener mSyncDeviceListener;

    private OnBackPressedListener mBackPressedListener = null;
    public void setOnBackListener(OnBackPressedListener listener) {
        mBackPressedListener = listener;
    }

    private OnCropImageCallback mCropImageCallback;
    public void setOnCropImageListener(OnCropImageCallback listener) {
        mCropImageCallback = listener;
    }

    protected void initMaxwellBleApi() {
        mMaxwellBLE = MGActivityTracker.getInstance(this);
        mMaxwellBLE.setDBDelegate(this);
        mMaxwellBLE.registerBTEventListener(this);
        mMaxwellBLE.registerActivityTrackerListener(this);
        mMaxwellBLE.registerAttributeListener(this);
    }

    protected void releaseMaxwellBleApi() {
        mMaxwellBLE.stopScan();
        mMaxwellBLE.unregisterBTEventListener(this);
        mMaxwellBLE.unregisterActivityTrackerListener(this);
        mMaxwellBLE.unregisterAttributeListener(this);
    }

    public void startScanDevice(OnPairDeviceListener listener) {
        mPairDeviceListener = listener;
        mMaxwellBLE.scan(0);
    }

    public void stopScanDevice() {
        mPairDeviceListener = null;
        mMaxwellBLE.stopScan();
    }

    public void setOnSyncDeviceListener(OnSyncDeviceListener listener) {
        mSyncDeviceListener = listener;
    }

    public void syncNow(boolean syncing) {
        mIsSyncing = syncing;
    }
    public boolean isSyncing() {
        return mIsSyncing;
    }

    public void setSyncProgress(double dProgress) {
        mSyncProgress = (int)(dProgress * 100.0d);
        if (mSyncProgress<0)
            mSyncProgress = 0;
        if (mSyncProgress>100)
            mSyncProgress = 100;
    }
    public int getSyncProgress() {
        return mSyncProgress;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        UtilDBG.d("MXWActivity > onActivityResult - requestCode = " + requestCode);

        // user is returning from capturing an image using the camera
        if (requestCode == MXWApp.CAMERA_CAPTURE && data != null) {

            Uri uri = data.getData();
            MXWApp.cropImage(this, uri, 256, 256);
        }
        else if (requestCode == MXWApp.GALLERY_PICK && data != null) {
            Uri uri = data.getData();
            MXWApp.cropImage(this, uri, 256, 256);
        }
        else if (requestCode == MXWApp.PIC_CROP) {
            //get the returned data
            if (data!=null) {
                Bundle extras = data.getExtras();

                // get the cropped bitmap
                if (extras!=null) {
                    Bitmap thePic = extras.getParcelable("data");
                    mCropImageCallback.onCropImageGot(thePic);
                }
            }
            return;
        }
    }

    @Override
    public void onBackPressed() {
        boolean bNoHandle = true;

        UtilDBG.d("MXWActivity > onBackPressed = " + mBackPressedListener);
        if (mBackPressedListener != null && mBackPressedListener.onBackPressed()) {
            bNoHandle = false;
        }

        // if no one handle this, ask user if sure to exit
        if (bNoHandle) {
            finish();
            /*DlgMessageYN dlg = new DlgMessageYN();

            dlg.setDes(getString(R.string.str_exit_app_sure))
                    .setPositiveButton(null, new DlgMessageYN.btnHandler() {
                        @Override
                        public boolean onBtnHandler() {
                            finish();
                            return true;
                        } })
                    .showHelper(this);*/
        }

        // do not call super.onBackPressed(); , it will cause the activity finish()
        // But we need to wait the BLE device disconnected successfully, and resources is clear
    }

    @Override
    public void onDeviceDiscover(MGPeripheral sender, AdvertisingData advertisingData) {
        UtilDBG.e("[RYAN] MXWActivity > onDeviceDiscover : device address : " + advertisingData.address);

        if (mPairDeviceListener != null) {
            mPairDeviceListener.onDeviceDiscover(sender, advertisingData);
        }
    }

    @Override
    public void onDeviceConnect(final MGPeripheral sender) {
        try{
            UtilDBG.e("[RYAN] MXWActivity > onDeviceConnect");

            if (mPairDeviceListener != null) {
                mPairDeviceListener.onDeviceConnect(sender);
            }

            if (mSyncDeviceListener != null) {
                mSyncDeviceListener.onDeviceConnect(sender);
            }
        }catch (Exception e){
            UtilDBG.e("[] MXWActivity > onDeviceConnect Exception: "+e);
        }

    }

    @Override
    public void onConnectTimeOut(MGPeripheral sender) {
        UtilDBG.i("[RYAN] MXWActivity > onConnectTimeOut !!!!!!!!");

        if (mPairDeviceListener != null) {
            mPairDeviceListener.onConnectTimeOut(sender);
        }

        if (mSyncDeviceListener != null) {
            mSyncDeviceListener.onConnectTimeOut(sender);
        }
    }

    @Override
    public void onSyncFinish() {
        UtilDBG.i("[RYAN] MXWActivity > onSyncFinish ");

        setSyncProgress(100);
        syncNow(false);

        if (mSyncDeviceListener != null) {
            mSyncDeviceListener.onSyncFinish();
        }
    }

    @Override
    public void onSyncFail() {
        UtilDBG.i("[RYAN] MXWActivity > onSyncFail ");

        syncNow(false);

        if (mSyncDeviceListener != null) {
            mSyncDeviceListener.onSyncFail();
        }
    }

    @Override
    public void onSyncProgressUpdate(int progress, int total) {
        UtilDBG.i("[RYAN] MXWActivity > onSyncProgressUpdate : " + progress + " | " + total);

        double dProgress = (double)progress / total;
        setSyncProgress(dProgress);

        if (mSyncDeviceListener != null) {
            mSyncDeviceListener.onSyncProgressUpdate(getSyncProgress());
        }
    }

    @Override
    public void onDeviceDisconnect(MGPeripheral sender, String s, int i) {
        UtilDBG.i("[RYAN] MXWActivity > onDeviceDisconnect !!!!!!!!");

        if (mSyncDeviceListener != null) {
            mSyncDeviceListener.onDeviceDisconnect(sender);
        }
    }
}
