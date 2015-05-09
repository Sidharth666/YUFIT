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
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilLocale;
import com.maxwell.bodysensor.util.UtilLocale.DateFmt;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.SharedPrefWrapper;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgTimePicker.btnHandler;
import com.maxwell.bodysensor.util.UtilTime;

public class DFPhoneNotify extends DFBase implements View.OnClickListener {

	private MainActivity mActivity;
	private SharedPrefWrapper mSharedPref;

	private Button mBtnNoDisturbingTimeStart, mBtnNoDisturbingTimeEnd;

    private boolean mEnableNoDisturbing;
	private int mStartTime, mEndTime;

	private enum TIME_APPLY_TO {
    	TIME_START, TIME_END
    }

    private OnPhoneNotifyChangedLitener mListener;
    public interface OnPhoneNotifyChangedLitener {
        public void onIncomingCallSave();
    }
    public void setOnPhoneNotifyChangedLitener(OnPhoneNotifyChangedLitener listener) {
        mListener = listener;
    }

	@Override
    public String getDialogTag() {
		return MainActivity.DF_PHONE_NOTIFY;
	}

	@Override
    public int getDialogTheme() {
		return R.style.app_df_trans_rr;
	}

	@Override
    public void saveData() {
        mSharedPref.enableInComingCallNoDisturbing(mEnableNoDisturbing);
        mSharedPref.setInComingCallNoDisturbingStart(mStartTime);
        mSharedPref.setInComingCallNoDisturbingEnd(mEndTime);

        if(mListener != null) {
            mListener.onIncomingCallSave();
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		UtilDBG.logMethod();

		mActivity = (MainActivity) getActivity();
		mSharedPref = SharedPrefWrapper.getInstance();

		View view = inflater.inflate(R.layout.df_phone_notify, container);

		// Set up the no disturbing switch.
        mEnableNoDisturbing = mSharedPref.isInComingCallNoDisturbingEnable();
		ToggleButton swtNoDisturbing = (ToggleButton) view.findViewById(R.id.swtNoDisturbing);
		swtNoDisturbing.setOnCheckedChangeListener(mSwtNoDisturbingOnCheckedChange);
		swtNoDisturbing.setChecked(mEnableNoDisturbing);

		// Set up button of no disturbing time start.
		mStartTime = mSharedPref.getInComingCallNoDisturbingStart();
		mBtnNoDisturbingTimeStart = (Button) view.findViewById(R.id.btnNoDisturbingTimeStart);
		updateTime(mBtnNoDisturbingTimeStart, mStartTime);

		// Set up button of no disturbing time end.
		mEndTime = mSharedPref.getInComingCallNoDisturbingEnd();
		mBtnNoDisturbingTimeEnd = (Button) view.findViewById(R.id.btnNoDisturbingTimeEnd);
		updateTime(mBtnNoDisturbingTimeEnd, mEndTime);

        setupTitleText(view, R.string.df_incoming_call_sms_notification_title);
		setupButtons(view);

		return view;
	}

	private void updateTime(Button btn, int time) {
        long ms = UtilTime.getMillisForIntTime(time);
        Date date = new Date(ms);

        btn.setText(UtilLocale.dateToString(date, DateFmt.HMa));
        btn.setOnClickListener(this);
	}

	private void showDlgTimePicker(final TIME_APPLY_TO when) {
		int time = 0;
		switch(when) {
		case TIME_START:
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
					case TIME_START:
						mStartTime = time;
						updateTime(mBtnNoDisturbingTimeStart, time);
						break;
					default:
						mEndTime = time;
						updateTime(mBtnNoDisturbingTimeEnd, time);
					}

					return true;
				}

        	});

        dlg.showHelper(mActivity);
	}

	private CompoundButton.OnCheckedChangeListener mSwtNoDisturbingOnCheckedChange = new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton button, boolean isChecked) {
            mEnableNoDisturbing = isChecked;
		}

	};

	@Override
	public void onClick(View v) {
		if (v==mBtnNoDisturbingTimeStart) {
			showDlgTimePicker(TIME_APPLY_TO.TIME_START);
		} else if (v==mBtnNoDisturbingTimeEnd) {
			showDlgTimePicker(TIME_APPLY_TO.TIME_END);
		}

	}

}
