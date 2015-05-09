package com.maxwell.bodysensor;

import android.os.Handler;

import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwellguider.bluetooth.MGPeripheral;

/**
 * Created by ryanhsueh on 15/4/21.
 */
public class EnergyCapsuleSync extends DeviceSync {

    public EnergyCapsuleSync(MainActivity activity) {
        super(activity);
    }

    @Override
    public void triggerSync() {
        if (!mMaxwellBLE.isReady()) {
            String mac = mPD.getTargetDeviceMac();
            mMaxwellBLE.setAutoConnect(false);
            mMaxwellBLE.connect(mac, 0);
        } else {
            doSync(DELAY_MS);
        }
    }

    @Override
    public void onDeviceReady(MGPeripheral sender) {
        doSync(0);
    }

    @Override
    public void onSyncFinish() {
        UtilDBG.i("[RYAN] EnergyCapsuleSync > onSyncFinish ");
        disconnectIt();
    }

    @Override
    public void onSyncFail() {
        UtilDBG.i("[RYAN] EnergyCapsuleSync > onSyncFail ");
        disconnectIt();
    }

    private void disconnectIt() {
        Handler handler = new Handler();
        handler.postDelayed(mRealDisconnect, DELAY_MS);
    }

    private Runnable mRealDisconnect =  new Runnable() {

        @Override
        public void run() {
            UtilDBG.i("[RYAN] EnergyCapsuleSync > mRealDisconnect ");
            mMaxwellBLE.disconnect();
        }
    };
}
