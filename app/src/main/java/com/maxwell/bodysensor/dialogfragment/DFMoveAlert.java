package com.maxwell.bodysensor.dialogfragment;

import java.util.Date;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.maxwell.bodysensor.dialogfragment.dialog.DlgTimePicker;
import com.maxwell.bodysensor.listener.OnSetupDeviceAlertListener;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilLocale;
import com.maxwell.bodysensor.util.UtilLocale.DateFmt;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.SharedPrefWrapper;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgTimePicker.btnHandler;
import com.maxwell.bodysensor.util.UtilTime;

public class DFMoveAlert extends DFBase implements View.OnClickListener {

	private MainActivity mActivity;
	private SharedPrefWrapper mSharedPref;

    private ToggleButton mToggleEnable;
	private Button mBtnMoveAlertTimeStart, mBtnMoveAlertTimeEnd;

    private boolean mEnable;
	private int mStartTime, mEndTime;

    private enum TIME_APPLY_TO {
    	MOVE_ALERT_TIME_START, MOVE_ALERT_TIME_END
    }

    private OnSetupDeviceAlertListener mListener;
    public void setDeviceAlertListener(OnSetupDeviceAlertListener listener) {
        mListener = listener;
    }

	@Override
    public String getDialogTag() {
		return MainActivity.DF_MOVE_ALERT;
	}

	@Override
    public int getDialogTheme() {
		return R.style.app_df_trans_rr;
	}

	@Override
    public void saveData() {
        mSharedPref.enableDeviceMoveAlert(mEnable);
		mSharedPref.setDeviceMoveAlertStart(mStartTime);
		mSharedPref.setDeviceMoveAlertEnd(mEndTime);

        if (mListener != null) {
            mListener.onDeviceAlertUpdated();
        }
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UtilDBG.logMethod();

        mActivity = (MainActivity) getActivity();
        mSharedPref = SharedPrefWrapper.getInstance();

        View view = inflater.inflate(R.layout.df_move_alert, container);

        // Set up the move alert switch based on app's database.
        mEnable = mSharedPref.isDeviceMoveAlertEnable();
        mToggleEnable = (ToggleButton) view.findViewById(R.id.toggle_move_alert);
        mToggleEnable.setChecked(mEnable);
        mToggleEnable.setOnCheckedChangeListener(OnMoveAlertCheckedChange);

        mBtnMoveAlertTimeStart = (Button) view.findViewById(R.id.btn_move_alert_time_start);
        mBtnMoveAlertTimeEnd = (Button) view.findViewById(R.id.btn_move_alert_time_end);

        // Get the start time
        mStartTime = mSharedPref.getDeviceMoveAlertStart();
        updateTime(mBtnMoveAlertTimeStart, mStartTime);

        // Get the end time
        mEndTime = mSharedPref.getDeviceMoveAlertEnd();
        updateTime(mBtnMoveAlertTimeEnd, mEndTime);

        setupTitleText(view, R.string.df_move_alert_title);
        setupButtons(view);

        return view;
	}

	private void updateTime(Button btn, int time) {
        long ms = UtilTime.getMillisForIntTime(time);
        Date date = new Date(ms);

        btn.setText(UtilLocale.dateToString(date, DateFmt.HMa));
        btn.setOnClickListener(this);
	}

	private CompoundButton.OnCheckedChangeListener OnMoveAlertCheckedChange =
			new CompoundButton.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                    mEnable = isChecked;
				}
			};

	private void showDlgTimePicker(final TIME_APPLY_TO when) {
		int time = 0;
		switch(when) {
		case MOVE_ALERT_TIME_START:
			time = mStartTime;
			break;
		default:
			time = mEndTime;
		}

		int hour = time / 60;
        int minute = time % 60;

		final DlgTimePicker dlg = new DlgTimePicker();
        dlg.showDatePicker(false);
        dlg.setTime(hour, minute)
        	.setPositiveButton(null, new btnHandler() {

				@Override
				public boolean onBtnHandler() {
					int hour = dlg.getCurrentHour();
					int minute = dlg.getCurrentMinute();
					int time = hour * 60 + minute;

					switch(when) {
					case MOVE_ALERT_TIME_START:
						mStartTime = time;
						updateTime(mBtnMoveAlertTimeStart, time);
						break;
					default:
						mEndTime = time;
						updateTime(mBtnMoveAlertTimeEnd, time);
					}

					return true;
				}

        	});

        dlg.showHelper(mActivity);
	}

	@Override
	public void onClick(View v) {
		if (v==mBtnMoveAlertTimeStart) {
			showDlgTimePicker(TIME_APPLY_TO.MOVE_ALERT_TIME_START);
		} else if (v==mBtnMoveAlertTimeEnd) {
			showDlgTimePicker(TIME_APPLY_TO.MOVE_ALERT_TIME_END);
		}
	}

}