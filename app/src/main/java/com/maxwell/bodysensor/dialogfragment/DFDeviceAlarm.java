package com.maxwell.bodysensor.dialogfragment;

import java.util.Date;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

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

public class DFDeviceAlarm extends DFBase implements View.OnClickListener {

	private MainActivity mActivity;
	private SharedPrefWrapper mSharedPref;

	private Button mBtnAlarmTime;
    private CheckBox mChkBoxEverySunday,
    				 mChkBoxEveryMonday,
    				 mChkBoxEveryTuesday,
    				 mChkBoxEveryWednesday,
    				 mChkBoxEveryThursday,
    				 mChkBoxEveryFriday,
    				 mChkBoxEverySaturday;

    private int mAlarmTime;

    private OnSetupDeviceAlertListener mListener;
    public void setDeviceAlertListener(OnSetupDeviceAlertListener listener) {
        mListener = listener;
    }

    public enum WEEKLY_ALARM {
        SUNDAY(1), MONDAY(2), TUESDAY(4), WEDNESDAY(8),
        THURSDAY(16), FRIDAY(32), SATURDAY(64), EVERYDAY(127);

        private int value;

        private WEEKLY_ALARM(int value){
            this.value = value;
        }

        public int getValue(){
            return this.value;
        }
    }

	@Override
    public String getDialogTag() {
		return MainActivity.DF_DEVICE_ALARM;
	}

	@Override
    public int getDialogTheme() {
		return R.style.app_df_trans_rr;
	}

	@Override
    public void saveData() {
		mSharedPref.setDeviceWeeklyAlarmTime(mAlarmTime);
		mSharedPref.setDeviceWeeklyAlarmMask(getWeeklyAlermMask());

        if (mListener != null) {
            mListener.onDeviceAlertUpdated();
        }
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UtilDBG.logMethod();

        mActivity = (MainActivity) getActivity();
        mSharedPref = SharedPrefWrapper.getInstance();

        View view = inflater.inflate(R.layout.df_device_alarm, container);

        mBtnAlarmTime = (Button) view.findViewById(R.id.btnAlarmTime);

		// Get the alarm time.
        mAlarmTime = mSharedPref.getDeviceWeeklyAlarmTime();
        updateTime(mBtnAlarmTime, mAlarmTime);

        mChkBoxEverySunday = (CheckBox) view.findViewById(R.id.chkBoxEverySunday);
        mChkBoxEveryMonday = (CheckBox) view.findViewById(R.id.chkBoxEveryMonday);
        mChkBoxEveryTuesday = (CheckBox) view.findViewById(R.id.chkBoxEveryTuesday);
        mChkBoxEveryWednesday = (CheckBox) view.findViewById(R.id.chkBoxEveryWednesday);
        mChkBoxEveryThursday = (CheckBox) view.findViewById(R.id.chkBoxEveryThursday);
        mChkBoxEveryFriday = (CheckBox) view.findViewById(R.id.chkBoxEveryFriday);
        mChkBoxEverySaturday = (CheckBox) view.findViewById(R.id.chkBoxEverySaturday);

        // Show the current setting.
        initWeeklyAlemMask();

        setupTitleText(view, R.string.df_alarm_title);
        setupButtons(view);

        return view;
	}

	private void updateTime(Button btn, int time) {
        long ms = UtilTime.getMillisForIntTime(time);
        Date date = new Date(ms);

        btn.setText(UtilLocale.dateToString(date, DateFmt.HMa));
        btn.setOnClickListener(this);
	}

	private void initWeeklyAlemMask() {
		int weeklyAlarmMask = mSharedPref.getDeviceWeeklyAlarmMask();

		// Check for Sunday.
		if ((weeklyAlarmMask & WEEKLY_ALARM.SUNDAY.getValue()) == WEEKLY_ALARM.SUNDAY.getValue())
			mChkBoxEverySunday.setChecked(true);
		else
			mChkBoxEverySunday.setChecked(false);

		// Check for Monday.
		if ((weeklyAlarmMask & WEEKLY_ALARM.MONDAY.getValue()) == WEEKLY_ALARM.MONDAY.getValue())
			mChkBoxEveryMonday.setChecked(true);
		else
			mChkBoxEveryMonday.setChecked(false);

		// Check for Tuesday.
		if ((weeklyAlarmMask & WEEKLY_ALARM.TUESDAY.getValue()) == WEEKLY_ALARM.TUESDAY.getValue())
			mChkBoxEveryTuesday.setChecked(true);
		else
			mChkBoxEveryTuesday.setChecked(false);

		// Check for Wednesday.
		if ((weeklyAlarmMask & WEEKLY_ALARM.WEDNESDAY.getValue()) == WEEKLY_ALARM.WEDNESDAY.getValue())
			mChkBoxEveryWednesday.setChecked(true);
		else
			mChkBoxEveryWednesday.setChecked(false);

		// Check for Thursday.
		if ((weeklyAlarmMask & WEEKLY_ALARM.THURSDAY.getValue()) == WEEKLY_ALARM.THURSDAY.getValue())
			mChkBoxEveryThursday.setChecked(true);
		else
			mChkBoxEveryThursday.setChecked(false);

		// Check for Friday.
		if ((weeklyAlarmMask & WEEKLY_ALARM.FRIDAY.getValue()) == WEEKLY_ALARM.FRIDAY.getValue())
			mChkBoxEveryFriday.setChecked(true);
		else
			mChkBoxEveryFriday.setChecked(false);

		// Check for Saturday.
		if ((weeklyAlarmMask & WEEKLY_ALARM.SATURDAY.getValue()) == WEEKLY_ALARM.SATURDAY.getValue())
			mChkBoxEverySaturday.setChecked(true);
		else
			mChkBoxEverySaturday.setChecked(false);
	}

	private int getWeeklyAlermMask() {
		int iDayCombination = 0;

        boolean isWeeklyChecked = false;
		if (mChkBoxEverySunday.isChecked()) {
            iDayCombination += WEEKLY_ALARM.SUNDAY.getValue();
            isWeeklyChecked = true;
        }
		if (mChkBoxEveryMonday.isChecked()) {
            iDayCombination += WEEKLY_ALARM.MONDAY.getValue();
            isWeeklyChecked = true;
        }
		if (mChkBoxEveryTuesday.isChecked()) {
            iDayCombination += WEEKLY_ALARM.TUESDAY.getValue();
            isWeeklyChecked = true;
        }
		if (mChkBoxEveryWednesday.isChecked()) {
            iDayCombination += WEEKLY_ALARM.WEDNESDAY.getValue();
            isWeeklyChecked = true;
        }
		if (mChkBoxEveryThursday.isChecked()) {
            iDayCombination += WEEKLY_ALARM.THURSDAY.getValue();
            isWeeklyChecked = true;
        }
		if (mChkBoxEveryFriday.isChecked()) {
            iDayCombination += WEEKLY_ALARM.FRIDAY.getValue();
            isWeeklyChecked = true;
        }
		if (mChkBoxEverySaturday.isChecked()) {
            iDayCombination += WEEKLY_ALARM.SATURDAY.getValue();
            isWeeklyChecked = true;
        }


        if (!isWeeklyChecked) {
            iDayCombination = WEEKLY_ALARM.EVERYDAY.getValue();
        }

		return iDayCombination;
	}

	private void showDlgTimePicker() {
		final DlgTimePicker dlg = new DlgTimePicker();
        dlg.showDatePicker(false);

		int time = mAlarmTime;

		int hour = time / 60;
        int minute = time % 60;

        dlg.setTime(hour, minute)
        	.setPositiveButton(null, new btnHandler() {

				@Override
				public boolean onBtnHandler() {
					int hour = dlg.getCurrentHour();
					int minute = dlg.getCurrentMinute();

					mAlarmTime = hour * 60 + minute;
					updateTime(mBtnAlarmTime, mAlarmTime);

					return true;
				}

        	});

        dlg.showHelper(mActivity);
	}

	@Override
	public void onClick(View v) {
		if (v==mBtnAlarmTime) {
			showDlgTimePicker();
		}
	}

}
