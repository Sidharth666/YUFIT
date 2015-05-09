package com.maxwell.bodysensor.dialogfragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.maxwell.bodysensor.dialogfragment.dialog.DlgTimePicker;
import com.maxwell.bodysensor.listener.OnSetupOutOfRangeListener;
import com.maxwell.bodysensor.util.UtilLocale;
import com.maxwell.bodysensor.util.UtilTime;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.SharedPrefWrapper;

import java.util.Date;

public class DFOutOfRangeAlert extends DFBase implements View.OnClickListener {

    private MainActivity mActivity;
	private SharedPrefWrapper mSharedPref;

    private Button mBtnNoDisturbingTimeStart, mBtnNoDisturbingTimeEnd;

    private boolean mEnableNoDisturbing;
    private int mStartTime, mEndTime;

    private enum TIME_APPLY_TO {
        TIME_START, TIME_END
    }

    private OnSetupOutOfRangeListener mListener;
    public void setOutOfRangeListener(OnSetupOutOfRangeListener listener) {
        mListener = listener;
    }

	@Override
    public String getDialogTag() {
		return MainActivity.DF_OUT_OF_RANGE_ALERT;
	}

	@Override
    public int getDialogTheme() {
		return R.style.app_df_trans_rr;
	}

	@Override
    public void saveData() {
        mSharedPref.enableOutOfRangeNoDisturbing(mEnableNoDisturbing);
        mSharedPref.setOutOfRangeNoDisturbingStart(mStartTime);
        mSharedPref.setOutOfRangeNoDisturbingEnd(mEndTime);

        if (mListener != null) {
            mListener.onOutOfRangeEnableChanged();
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		UtilDBG.logMethod();

        mActivity = (MainActivity) getActivity();
        mSharedPref = SharedPrefWrapper.getInstance();

		View view = inflater.inflate(R.layout.df_out_of_range_alert, container);

        // Set up the no disturbing switch.
        mEnableNoDisturbing = mSharedPref.isOutOfRangeNoDisturbingEnable();
        ToggleButton swtNoDisturbing = (ToggleButton) view.findViewById(R.id.swtNoDisturbing);
        swtNoDisturbing.setOnCheckedChangeListener(mSwtNoDisturbingOnCheckedChange);
        swtNoDisturbing.setChecked(mEnableNoDisturbing);

        // Set up button of no disturbing time start.
        mStartTime = mSharedPref.getOutOfRangeNoDisturbingStart();
        mBtnNoDisturbingTimeStart = (Button) view.findViewById(R.id.btnNoDisturbingTimeStart);
        updateTime(mBtnNoDisturbingTimeStart, mStartTime);

        // Set up button of no disturbing time end.
        mEndTime = mSharedPref.getOutOfRangeNoDisturbingEnd();
        mBtnNoDisturbingTimeEnd = (Button) view.findViewById(R.id.btnNoDisturbingTimeEnd);
        updateTime(mBtnNoDisturbingTimeEnd, mEndTime);

        setupTitleText(view, R.string.df_out_of_range_title);
		setupButtons(view);

		return view;
	}

    private void updateTime(Button btn, int time) {
        long ms = UtilTime.getMillisForIntTime(time);
        Date date = new Date(ms);

        btn.setText(UtilLocale.dateToString(date, UtilLocale.DateFmt.HMa));
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
                .setPositiveButton(null, new DlgTimePicker.btnHandler() {

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
