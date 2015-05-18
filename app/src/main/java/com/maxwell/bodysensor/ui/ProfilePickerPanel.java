package com.maxwell.bodysensor.ui;

import java.util.Calendar;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.util.UtilCVT;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;

public class ProfilePickerPanel implements View.OnClickListener,
	DatePicker.OnDateChangedListener, NumberPicker.OnValueChangeListener {

    public enum ProfilePicker {
        BIRTHDAY,
        HEIGHT_METRIC,
        HEIGHT_IMPERIAL,
        STRIDE_METRIC,
        STRIDE_IMPERIAL,
        WEIGHT_METRIC,
        WEIGHT_IMPERIAL,
        SLEEP_START,
        SLEEP_WAKE
    }

	private static final int MAX_HEIGHT_CM = 300;
	private static final int MIN_HEIGHT_CM = 50;
	private static final int MAX_STRIDE_CM = 150;
	private static final int MIN_STRIDE_CM = 0;
	private static final int MAX_WEIGHT_KG = 300;
	private static final int MIN_WEIGHT_KG = 2;

	private static final int MAX_INCH = 11;
	private static final int MIN_INCH = 0;
	private static final int MAX_HEIGHT_FT = 9;
	private static final int MIN_HEIGHT_FT = 0;
	private static final int MAX_STRIDE_FT = 4;
	private static final int MIN_STRIDE_FT = 0;
	private static final int MAX_WEIGHT_LBS = 1000;
	private static final int MIN_WEIGHT_LBS = 4;

	private View mPanel;
	private View mAnim;
	private TextView mTextTitle;
	private Button mBtnCancel, mBtnDone;

	private DatePicker mPickerBirthday;
	private View mViewPanelCM;
	private NumberPicker mPickerCM;
	private View mViewPanelIn;
	private NumberPicker mPickerFT, mPickerInch;
	private View mViewPanelWeight;
	private NumberPicker mPickerWeight;
	private TextView mTextWeightDes;
	private TimePicker mPickerTime;

	private ProfilePicker mPickerType = ProfilePicker.BIRTHDAY;
	private int mYear, mMonthOfYear, mDayOfMonth;
	private int mMeter, mFt, mInch, mWeight;

	private OnProfileChangedListener mListener;
	public interface OnProfileChangedListener {
		void onBirthdayDone(int year, int monthOfYear, int dayOfMonth);
		void onHeightDone(int cm);
		void onHeightDone(int ft, int inch);
		void onStrideDone(int cm);
		void onStrideDone(int ft, int inch);
		void onWeightKgDone(int kg);
		void onWeightLbsDone(int lbs);
		void onSleepStartDone(int hourOfDay, int minute);
		void onSleepWakeDone(int hourOfDay, int minute);
	}

	public void setListener(OnProfileChangedListener listener) {
		mListener = listener;
	}

	public ProfilePickerPanel(View rootView) {
		mPanel = rootView.findViewById(R.id.layout_profile_picker);
		mAnim = rootView.findViewById(R.id.animation);

		mPickerBirthday = (DatePicker) rootView.findViewById(R.id.picker_birthday);

		mViewPanelCM = rootView.findViewById(R.id.picker_panel_cm);
		mPickerCM = (NumberPicker) rootView.findViewById(R.id.picker_cm);
		mPickerCM.setOnValueChangedListener(this);

		mViewPanelIn = rootView.findViewById(R.id.picker_panel_in);
		mPickerFT = (NumberPicker) rootView.findViewById(R.id.picker_ft);
		mPickerFT.setOnValueChangedListener(this);
		mPickerInch = (NumberPicker) rootView.findViewById(R.id.picker_inch);
		mPickerInch.setOnValueChangedListener(this);

		mViewPanelWeight = rootView.findViewById(R.id.picker_panel_weight);
		mPickerWeight = (NumberPicker) rootView.findViewById(R.id.picker_weight);
		mPickerWeight.setOnValueChangedListener(this);
		mTextWeightDes = (TextView) rootView.findViewById(R.id.picker_weight_des);

		mPickerTime = (TimePicker) rootView.findViewById(R.id.picker_time);

		mTextTitle = (TextView) rootView.findViewById(R.id.text_picker_title);

		mBtnCancel = (Button) rootView.findViewById(R.id.btn_cancel);
		mBtnCancel.setOnClickListener(this);

		mBtnDone = (Button) rootView.findViewById(R.id.btn_done);
		mBtnDone.setOnClickListener(this);
	}

	public boolean isVisible() {
		return (mPanel.getVisibility() == View.VISIBLE);
	}

	public void show(ProfilePicker type, long defVal) {
		mPickerType = type;

		mPanel.setVisibility(View.VISIBLE);

		Animation animation = new TranslateAnimation(0,0,1000,0);
        animation.setDuration(333);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation a) {
            }

            @Override
            public void onAnimationRepeat(Animation a) {
            }

            @Override
            public void onAnimationStart(Animation a) {
            }
        });

        mAnim.startAnimation(animation);

		int inch;
		switch(type) {
		case BIRTHDAY:
			mTextTitle.setText(R.string.birthday);

			UtilCalendar cal = new UtilCalendar(defVal, null/*UtilTZ.getUTCTZ()*/);
			mYear = cal.get(Calendar.YEAR);
			mMonthOfYear = cal.get(Calendar.MONTH);
			mDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
			mPickerBirthday.init(mYear, mMonthOfYear, mDayOfMonth, this);
			mPickerBirthday.setVisibility(View.VISIBLE);
			break;
		case HEIGHT_METRIC:
			mTextTitle.setText(R.string.cpHeight);

			mMeter = (int) defVal;

			// range : 50cm ~ 300cm
			initPickerCM(mMeter, MIN_HEIGHT_CM, MAX_HEIGHT_CM);
			break;
		case HEIGHT_IMPERIAL:
			mTextTitle.setText(R.string.cpHeight);

			inch = UtilCVT.doubleToInt(UtilCVT.cmToInch(defVal));
			mFt = inch / 12;
			mInch = inch % 12;

			// range : 1'0" ~ 9'11"
			initPickerFtIn(mFt, MIN_HEIGHT_FT, MAX_HEIGHT_FT, mInch, MIN_INCH, MAX_INCH);
			break;
		case STRIDE_METRIC:
			mTextTitle.setText(R.string.cpStride);

			mMeter = (int) defVal;

			// range : 0cm ~ 150cm
			initPickerCM(mMeter, MIN_STRIDE_CM, MAX_STRIDE_CM);
			break;
		case STRIDE_IMPERIAL:
			mTextTitle.setText(R.string.cpStride);

			inch = UtilCVT.doubleToInt(UtilCVT.cmToInch(defVal));
			mFt = inch / 12;
			mInch = inch % 12;

			// range : 0'0" ~ 4'11"
			initPickerFtIn(mFt, MIN_STRIDE_FT, MAX_STRIDE_FT, mInch, MIN_INCH, MAX_INCH);
			break;
		case WEIGHT_METRIC:
			mTextTitle.setText(R.string.cpWeight);

			mWeight = (int) defVal;

			// range : 2kg ~ 400kg
			initPickerWeight(mWeight, MIN_WEIGHT_KG, MAX_WEIGHT_KG, true);
			break;
		case WEIGHT_IMPERIAL:
			mTextTitle.setText(R.string.cpWeight);

			mWeight = UtilCVT.doubleToInt(UtilCVT.kgToLb(defVal));

			// range : 4lbs ~ 1000lbs
			initPickerWeight(mWeight, MIN_WEIGHT_LBS, MAX_WEIGHT_LBS, false);
			break;
		case SLEEP_START:
			UtilDBG.d("[RYAN] defVal = " + defVal);
			mTextTitle.setText(R.string.sleep_start_time);

			initTimePicker((int)defVal);
			break;
		case SLEEP_WAKE:
			mTextTitle.setText(R.string.sleep_wake_time);

			initTimePicker((int)defVal);
			break;
		}

	}

	private void initPickerCM(int defVal, int min, int max) {
	    mViewPanelCM.setVisibility(View.VISIBLE);
		mPickerCM.setMinValue(min);
		mPickerCM.setMaxValue(max);
		mPickerCM.setValue(defVal);
		mPickerCM.setWrapSelectorWheel(false);
	}

	private void initPickerFtIn(int ftDefVal, int ftMin, int ftMax, int inDefVal, int inMin, int inMax) {
	    mViewPanelIn.setVisibility(View.VISIBLE);

		mPickerFT.setMinValue(ftMin);
		mPickerFT.setMaxValue(ftMax);
		mPickerFT.setValue(ftDefVal);
		mPickerFT.setWrapSelectorWheel(false);

	    mPickerInch.setMinValue(inMin);
	    mPickerInch.setMaxValue(inMax);
	    mPickerInch.setValue(inDefVal);
	    mPickerInch.setWrapSelectorWheel(false);
	}

	private void initPickerWeight(int defVal, int min, int max, boolean bKg) {
	    mViewPanelWeight.setVisibility(View.VISIBLE);
		mPickerWeight.setMinValue(min);
		mPickerWeight.setMaxValue(max);
		mPickerWeight.setValue(defVal);
		mPickerWeight.setWrapSelectorWheel(false);
		mTextWeightDes.setText(bKg ? R.string.cpKg : R.string.cpLbs);
	}

	private void initTimePicker(int defVal) {
		mPickerTime.setCurrentHour(defVal / 60);
		mPickerTime.setCurrentMinute(defVal % 60);
		mPickerTime.setVisibility(View.VISIBLE);
	}

	public void hide() {
		Animation animation = new TranslateAnimation(0,0,0,1000);
        animation.setDuration(333);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation a) {
                mPickerBirthday.setVisibility(View.GONE);
                mViewPanelCM.setVisibility(View.GONE);
                mViewPanelIn.setVisibility(View.GONE);
                mViewPanelWeight.setVisibility(View.GONE);
                mPickerTime.setVisibility(View.GONE);

                mPanel.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation a) {
            }

            @Override
            public void onAnimationStart(Animation a) {
            }
        });

        mAnim.startAnimation(animation);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_cancel :
			break;
		case R.id.btn_done :
			if (mListener == null) break;

            switch (mPickerType) {
                case BIRTHDAY:
                    mListener.onBirthdayDone(mYear, mMonthOfYear, mDayOfMonth);
                    break;
                case HEIGHT_METRIC:
                    mListener.onHeightDone(mMeter);
                    break;
                case HEIGHT_IMPERIAL:
                    mListener.onHeightDone(mFt, mInch);
                    break;
                case STRIDE_METRIC:
                    mListener.onStrideDone(mMeter);
                    break;
                case STRIDE_IMPERIAL:
                    mListener.onStrideDone(mFt, mInch);
                    break;
                case WEIGHT_METRIC:
                    mListener.onWeightKgDone(mWeight);
                    break;
                case WEIGHT_IMPERIAL:
                    mListener.onWeightLbsDone(mWeight);
                    break;
                case SLEEP_START:
                    mListener.onSleepStartDone(mPickerTime.getCurrentHour(), mPickerTime.getCurrentMinute());
                    break;
                case SLEEP_WAKE:
                    mListener.onSleepWakeDone(mPickerTime.getCurrentHour(), mPickerTime.getCurrentMinute());
                    break;
            }
			break;
		}

		hide();
	}

	@Override
	public void onDateChanged(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		mYear = year;
		mMonthOfYear = monthOfYear;
		mDayOfMonth = dayOfMonth;
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		switch(picker.getId()) {
		case R.id.picker_cm :
			mMeter = newVal;
			break;
		case R.id.picker_ft :
			mFt = newVal;
			break;
		case R.id.picker_inch :
			mInch = newVal;
			break;
		case R.id.picker_weight :
			mWeight = newVal;
			break;
		}

	}
}
