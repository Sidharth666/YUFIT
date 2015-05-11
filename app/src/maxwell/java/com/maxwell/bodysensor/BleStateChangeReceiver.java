package com.maxwell.bodysensor;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by root on 11/5/15.
 */
public class BleStateChangeReceiver extends BroadcastReceiver {

    private DBProgramData mPD;

    @Override
    public void onReceive(Context context, Intent intent) {
        UtilDBG.d("[] BleStateChangeReceiver >>> onReceive !!");

        String action = intent.getAction();

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

            if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                mPD = DBProgramData.getInstance();
                String address = mPD.getTargetDeviceMac();
                UtilDBG.d("[] BleStateChangeReceiver >>> onReceive !!  "+intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1));

                if (MXWApp.initBleAutoConnection(address)) {
                    MXWApp.connectDevice(mPD.getTargetDeviceMac());
                }
            }

        }
    }
}
