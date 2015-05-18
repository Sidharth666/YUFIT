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
    private static final String ACTION = "com.healthifyme.ACTION_YUFIT_SYNC_FAILED";
    private boolean isReady,isConnected,isBtOn;


    @Override
    public void onReceive(Context context, Intent intent) {
        mMaxwellBLE = MGActivityTracker.getInstance(context);
        isReady = mMaxwellBLE.isReady();
        isConnected = mMaxwellBLE.isConnected();
        isBtOn = mMaxwellBLE.isBtOn();

        if(isReady){
            UtilDBG.e("SyncStartReceiver, intent, sync");
            mMaxwellBLE.sync();
        }else{

            Intent intentHM = new Intent(ACTION);
            intentHM.putExtra("isReady",isReady);
            intentHM.putExtra("isConnected",isConnected);
            intentHM.putExtra("isBtOn",isBtOn);
            context.sendBroadcast(intentHM);
        }

    }
}
