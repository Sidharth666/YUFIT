package com.maxwell.bodysensor;

import android.os.Handler;

import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.ui.WarningUtil;
import com.maxwell.bodysensor.util.UtilCVT;
import com.maxwellguider.bluetooth.MGPeripheral;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;

/**
 * Created by ryanhsueh on 15/4/21.
 */
public abstract class DeviceSync {

    public final int DELAY_MS = 500;

    private MainActivity mActivity;

    protected DBProgramData mPD;
    protected MGActivityTrackerApi mMaxwellBLE;

    private OnDeviceSyncListener mListener;
    public interface OnDeviceSyncListener {
        void updateConnectionImageForSync(boolean syncing);
        void updateSyncProgress(double progress);
    }
    public void setOnDeviceSyncListener(OnDeviceSyncListener listener) {
        mListener = listener;
    }

    public DeviceSync(MainActivity activity) {
        mActivity = activity;
        mPD = DBProgramData.getInstance();
        mMaxwellBLE = MGActivityTracker.getInstance(mActivity);
    }

    public void release() {
        mListener = null;
    }

    protected void doSync(long delay) {
        Handler handler = new Handler();
        handler.postDelayed(mRealSyncADT, delay);
    }

    private void syncADT() {
        if (mActivity.isSyncing()) {
            return;
        }

        String targetMac = mPD.getTargetDeviceMac();
        boolean bFocusEC = (UtilCVT.getMacAddressType(targetMac) == 2) && (mPD.getUserDeviceByAddress(targetMac)!=null);
        if (bFocusEC) {

            if (mMaxwellBLE.isReady()) {

                if (mListener != null) {
                    mListener.updateConnectionImageForSync(true);
                    mListener.updateSyncProgress(0.0d);
                }

                // Start sync
                mActivity.syncNow(true);
                mMaxwellBLE.sync();

            } else {
                WarningUtil.showToastLong(mActivity, R.string.dlg_device_disconnectd_content);
            }

        }
    }


    public abstract void triggerSync();
    public abstract void onDeviceReady(MGPeripheral sender);
    public abstract void onSyncFinish();
    public abstract void onSyncFail();


    private Runnable mRealSyncADT =  new Runnable() {

        @Override
        public void run() {
            if (mActivity.isActivityOnResumed()) {
                syncADT();
            }
        }
    };

}
