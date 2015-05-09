package com.maxwell.bodysensor.listener;

import android.bluetooth.BluetoothGatt;

import com.maxwellguider.bluetooth.AdvertisingData;
import com.maxwellguider.bluetooth.MGPeripheral;

/**
 * Created by ryanhsueh on 15/4/8.
 */
public interface OnPairDeviceListener {

    public void onDeviceDiscover(MGPeripheral sender, AdvertisingData advertisingData);
    public void onDeviceConnect(MGPeripheral sender);
    public void onConnectTimeOut(MGPeripheral sender);
    public void onDeviceReady(MGPeripheral sender);
}
