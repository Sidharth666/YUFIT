package com.maxwell.bodysensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Display;
import android.view.WindowManager;

import com.maxwell.bodysensor.fragment.FCamera;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by ryanhsueh on 15/4/28.
 */
public class CameraActivity extends FragmentActivity {

    public static final String ACTION_TAKE_PICTURE = "com.maxwell.camera.action.TAKE_PICTURE";
    public static final String ACTION_VIDEO_RECORDER = "com.maxwell.camera.action.VIDEO_RECORDER";

    public static final String KEY_IS_VIDEO_RECORDER = "key_is_video_recorder";
    private FCamera mFCamera;
    int orientation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (ft==null) {
            UtilDBG.e("showContainerFirst, can not get FragmentTransaction");
            return;
        }
        Intent intent = getIntent();
        if (intent.getAction().equals(ACTION_VIDEO_RECORDER)) {
            intent.putExtra(KEY_IS_VIDEO_RECORDER, true);
        } else {
            intent.putExtra(KEY_IS_VIDEO_RECORDER, false);
        }
        mFCamera = new FCamera();
        mFCamera.setArguments(intent.getExtras());
        ft.add(R.id.container_camera, mFCamera, "ContainerCamera");
        ft.show(mFCamera);
        ft.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
    }
    public int getScreenOrientation()
    {
        Display getOrient = getWindowManager().getDefaultDisplay();
        if(getOrient.getWidth()==getOrient.getHeight()){
            orientation = Configuration.ORIENTATION_SQUARE;
        } else{
            if(getOrient.getWidth() < getOrient.getHeight()){
                orientation = Configuration.ORIENTATION_PORTRAIT;
            }else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }
    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MXWApp.ACTION_SMART_KEY_CAMERA);
        registerReceiver(mSmartKeyReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(mSmartKeyReceiver);
    }

    private BroadcastReceiver mSmartKeyReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            UtilDBG.e("[RYAN] mSmartKeyReceiver > onReceive > action : " + intent.getAction());
            if (intent.getAction().equals(MXWApp.ACTION_SMART_KEY_CAMERA)) {
                if (mFCamera != null) {
                    mFCamera.onSmartKeyTrigged();
                }
            }
        }
    };

}
