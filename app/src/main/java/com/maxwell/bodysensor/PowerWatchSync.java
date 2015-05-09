package com.maxwell.bodysensor;

import com.maxwellguider.bluetooth.MGPeripheral;

/**
 * Created by ryanhsueh on 15/4/21.
 */
public class PowerWatchSync extends DeviceSync {

    public PowerWatchSync(MainActivity activity) {
        super(activity);
    }

    @Override
    public void triggerSync() {
        doSync(DELAY_MS);
    }

    @Override
    public void onDeviceReady(MGPeripheral sender) {
        doSync(DELAY_MS);
    }

    @Override
    public void onSyncFinish() {

    }

    @Override
    public void onSyncFail() {

    }
}
