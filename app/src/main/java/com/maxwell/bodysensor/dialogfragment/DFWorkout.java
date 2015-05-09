package com.maxwell.bodysensor.dialogfragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxwell.bodysensor.MXWApp;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.listener.OnFitnessUpdateListener;
import com.maxwell.bodysensor.ui.ViewWorkoutProgress;
import com.maxwell.bodysensor.ui.WarningUtil;
import com.maxwell.bodysensor.util.UtilConst;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwellguider.bluetooth.activitytracker.FitnessType;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;

import java.util.Date;

/**
 * Created by ryanhsueh on 15/2/5.
 */
public class DFWorkout extends DFBase implements OnFitnessUpdateListener {

    public final static int MSG_BT_DEVICE_START_FITNESS = 1;
    public final static int MSG_BT_DEVICE_STOP_FITNESS = 2;

    private final static int REPS = 20;
    private final static int ONE_KM_FROM_CM = 100000;

    private MainActivity mActivity;
    private DBProgramData mPD;
    private MGActivityTrackerApi mMaxwellBLE;

    private ImageView mImgFitnessType;
    private TextView mTxtMoveCount;
    private TextView mTxtVelocity;
    private TextView mTxtBestRecord;
    private ImageButton mImgBtnStartStop;
    private boolean mbStarted = false;

    private FitnessType mFitnessType;
    private ViewWorkoutProgress mWorkoutProgress;

    private int miFirstMoveCount = UtilConst.INVALID_INT;  // Keep the base of Move count before reset.
    private int miPrevMoveCount = UtilConst.INVALID_INT;   // Keep the base of Move count after reset.
    private int miMoveCountAccum = UtilConst.INVALID_INT;
    private int miMoveCountHit = UtilConst.INVALID_INT;
    private double mdDistanceCM = UtilConst.INVALID_INT;

    private Date mStartDate;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BT_DEVICE_START_FITNESS:
                    if (startFitnessPlay()) {
                        mImgBtnStartStop.setImageResource(R.drawable.fp_img_btn_stop_selector);
                    } else {
                        stopFitnessPlay();
                        mImgBtnStartStop.setImageResource(R.drawable.fp_img_btn_start_selector);
                    }
                    break;
                case MSG_BT_DEVICE_STOP_FITNESS:
                    stopFitnessPlay();
                    mImgBtnStartStop.setImageResource(R.drawable.fp_img_btn_start_selector);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    };

    @Override
    public String getDialogTag() {
        return MainActivity.DF_WORKOUT;
    }

    @Override
    public int getDialogTheme() {
        return R.style.app_df_trans_bb;
    }

    @Override
    public void saveData() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UtilDBG.logMethod();

        mActivity = (MainActivity) getActivity();
        mPD = DBProgramData.getInstance();
        mMaxwellBLE = MGActivityTracker.getInstance(mActivity);

        mActivity.setOnFitnessMoveListener(this);

        View view = inflater.inflate(R.layout.df_workout, container);

        mWorkoutProgress = (ViewWorkoutProgress) view.findViewById(R.id.progress_workout);

        mImgFitnessType = (ImageView) view.findViewById(R.id.img_fitness_type);
        mTxtMoveCount = (TextView) view.findViewById(R.id.text_move_count);
        mTxtVelocity = (TextView) view.findViewById(R.id.text_velocity);
        mTxtBestRecord = (TextView) view.findViewById(R.id.text_best_record);

        // TODO : What is best record???
        mTxtVelocity.setText(String.format(getString(R.string.velocity_per_min), 0));
        mTxtBestRecord.setText(String.valueOf(53));

        mImgBtnStartStop = (ImageButton) view.findViewById(R.id.imgBtnStartStop);
        mImgBtnStartStop.setOnClickListener(imgBtnStartStopOnClick);

        if (mFitnessType != null) {
//            mMaxwellBLE.configFitnessFilter(mFitnessType);

            mWorkoutProgress.setFitnessType(mFitnessType);

            switch(mFitnessType) {
                case JUMPING_ROPE:
                    mImgFitnessType.setImageResource(R.drawable.jumprope);
                    break;
                case JUMPING_JACK:
                    mImgFitnessType.setImageResource(R.drawable.jumpjack);
                    break;
                case SIT_UP:
                    mImgFitnessType.setImageResource(R.drawable.situp);
                    break;
                case TREADMILL:
                    mImgFitnessType.setImageResource(R.drawable.treadmill);
                    break;
            }
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

//        mMaxwellBLE.configFitnessFilter(FitnessType.UNKNOWN);

        mHandler.sendEmptyMessage(MSG_BT_DEVICE_STOP_FITNESS);
        mActivity.setOnFitnessMoveListener(null);

        if (mWorkoutProgress != null) {
            mWorkoutProgress.releaseResource();
            mWorkoutProgress = null;
        }
    }

    public void setFitnessType(FitnessType type) {
        mFitnessType = type;
    }

    private void resetMoveCount() {
        miFirstMoveCount = UtilConst.INVALID_INT;
        miPrevMoveCount = UtilConst.INVALID_INT;
        miMoveCountAccum = UtilConst.INVALID_INT;
    }

    private boolean startFitnessPlay() {
        if (mActivity.isSyncing()) {
            return false;
        }

        if (mMaxwellBLE.isReady()) {
            mbStarted = true;
            if (mFitnessType == FitnessType.TREADMILL) {
                mMaxwellBLE.readCurrentHourStep();
            } else {
                mMaxwellBLE.readCurrentHourMove();
            }
        } else {
            showDlgDeviceNotConnected();
            mbStarted = false;
        }

        return mbStarted;
    }

    private void stopFitnessPlay() {
        mbStarted = false;
    }

    private void updateMoveCount(int move) {
        if (miFirstMoveCount == UtilConst.INVALID_INT) {  // 1st received value.
            miFirstMoveCount = move;  // Base of Move count before reset.
            miPrevMoveCount = 0;
            miMoveCountAccum = 0;  // Accumulate the Move count after the 1st received value.
            miMoveCountHit = 0;
            mdDistanceCM = 0;
        } else {  // Not the 1st value.
            // The Move count of the Bluetooth device is reset to zero for a time period.
            // We check it and do accumulation accordingly.
            if (move >= miMoveCountAccum)  // Not reset yet.
                miMoveCountAccum = move - miFirstMoveCount;
            else {  // The Move count of the Bluetooth device has been reset to zero.
                if (move < miPrevMoveCount)  // Move count is reset again.
                    miPrevMoveCount = 0;

                miMoveCountAccum += move - miPrevMoveCount;
                miPrevMoveCount = move;
            }
        }

        if (mFitnessType == FitnessType.TREADMILL) {
            miMoveCountHit = miMoveCountAccum;
            mdDistanceCM = (double)miMoveCountHit * mPD.getPersonStride();
            mTxtMoveCount.setText(String.format("%.2fkm", mdDistanceCM/ONE_KM_FROM_CM));
        } else {
            miMoveCountHit = miMoveCountAccum;
            mTxtMoveCount.setText(String.valueOf(miMoveCountHit));
        }
        UtilDBG.i("[RYAN] DFWorkout > updateMoveCount > miMoveCountAccum = " + miMoveCountAccum);

        
        // calculate velocity
        float duration = (float) (System.currentTimeMillis() - mStartDate.getTime());
        float movePerSec = (float)miMoveCountHit / (duration/1000);
        int velocity = (int)(movePerSec * 60); // move/min
        mTxtVelocity.setText(String.format(getString(R.string.velocity_per_min), velocity));
    }

    private void showDlgDeviceNotConnected() {
        WarningUtil.showDFMessageOK(getActivity(), 0, R.string.dlg_device_disconnectd_content);
    }

    private View.OnClickListener imgBtnStartStopOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (MXWApp.isClickFast(v)) {
                return;
            }

            if (mbStarted) {
                mHandler.removeMessages(MSG_BT_DEVICE_START_FITNESS);
                mHandler.sendEmptyMessage(MSG_BT_DEVICE_STOP_FITNESS);
            } else {
                mStartDate = new Date();

                resetMoveCount();
                mWorkoutProgress.setProgress(0);
                mHandler.sendEmptyMessageDelayed(MSG_BT_DEVICE_START_FITNESS, 200);
            }

            mMaxwellBLE.readFitnessFilter();
        }
    };

    @Override
    public void onMoveUpdate(final int move) {
        UtilDBG.d("[RYAN] DFWorkout > onMoveUpdate : " + move);

        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mbStarted) {
                    updateMoveCount(move);
                    int progress = miMoveCountHit / REPS;

                    mWorkoutProgress.setProgress(progress);
                }
            }
        });

        if (mbStarted) {
            mHandler.sendEmptyMessageDelayed(MSG_BT_DEVICE_START_FITNESS, 500);
        }
    }

    @Override
    public void onStepUpdate(final int step) {
        UtilDBG.d("[RYAN] DFWorkout > onStepUpdate : " + step);

        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mbStarted) {
                    updateMoveCount(step);
                    int progress = (int)(mdDistanceCM / ONE_KM_FROM_CM);

                    mWorkoutProgress.setProgress(progress);
                }
            }
        });

        if (mbStarted) {
            mHandler.sendEmptyMessageDelayed(MSG_BT_DEVICE_START_FITNESS, 500);
        }
    }
}
