package com.maxwell.bodysensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by ryanhsueh on 15/4/28.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    private DBProgramData mPD;

    @Override
    public void onReceive(Context context, Intent intent) {
        UtilDBG.d("[RYAN] BootCompletedReceiver >>> onReceive !!");
        mPD = DBProgramData.getInstance();
        String address = mPD.getTargetDeviceMac();

        if (MXWApp.initBleAutoConnection(address)) {
            MXWApp.connectDevice(mPD.getTargetDeviceMac());
        }
    }

}
