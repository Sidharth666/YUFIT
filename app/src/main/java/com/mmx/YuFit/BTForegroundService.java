package com.mmx.YuFit;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.maxwell.bodysensor.MXWApp;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.DBProgramData;

public class BTForegroundService extends Service {
    private DBProgramData mPD;
    private static final int NOTIFICATION_ID = 1;

    public BTForegroundService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

    //start foreground, cannot be killed.
        startForeground(NOTIFICATION_ID,getNotification());

        mPD = DBProgramData.getInstance();
        String address = mPD.getTargetDeviceMac();

        if (MXWApp.initBleAutoConnection(address)) {
//            MXWApp.connectDevice(mPD.getTargetDeviceMac());
        }

        MXWApp.IS_STARTED = true;

        return START_STICKY;

    }

    private Notification getNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.yufit_logo_titlebar).setContentTitle("YUFIT").setWhen(System.currentTimeMillis());
        Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1, startIntent,0);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        return notification;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
