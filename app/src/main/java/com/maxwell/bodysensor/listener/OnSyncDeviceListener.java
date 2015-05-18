package com.maxwell.bodysensor.listener;

import com.maxwellguider.bluetooth.MGPeripheral;

/**
 * Created by ryanhsueh on 15/4/8.
 */
public interface OnSyncDeviceListener {

    public void onDeviceConnect(MGPeripheral sender);
    public void onDeviceDisconnect(MGPeripheral sender);
    public void onConnectTimeOut(MGPeripheral sender);
    public void onDeviceReady(MGPeripheral sender);
    public void onSyncProgressUpdate(int progress);
    public void onSyncFinish();
    public void onSyncFail();


}
