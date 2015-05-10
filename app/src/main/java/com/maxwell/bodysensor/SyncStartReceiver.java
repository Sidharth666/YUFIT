package com.maxwell.bodysensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.maxwell.bodysensor.listener.OnSyncDeviceListener;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwellguider.bluetooth.MGPeripheral;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;

/**
 * Created by root on 10/5/15.
 */
public class SyncStartReceiver extends BroadcastReceiver {

    private MGActivityTrackerApi mMaxwellBLE;

    @Override
    public void onReceive(Context context, Intent intent) {
        mMaxwellBLE = MGActivityTracker.getInstance(context);
        if(mMaxwellBLE.isConnected()){
            UtilDBG.e("SyncStartReceiver, intent, sync");
            mMaxwellBLE.sync();
        }else{

//            sendtoHMIntent.putExtra("","");
//            context.sendBroadcast(sendtoHMIntent);
        }

    }
}
