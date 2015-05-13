package com.maxwell.bodysensor.dialogfragment.dialog;

import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.dialogfragment.DFBase;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.MainActivity;


public class DlgTimePicker extends DFBase implements
    View.OnClickListener {

    private TextView mTextTitle;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private Button mBtnCancel;
    private Button mBtnOK;

    private String mStringTitle = null;
    private String mStringCancel = null;
    private String mStringOK = null;
    private int mYear = -1;
    private int mMonth = -1;
    private int mDay = -1;
    private int mHour = -1;
    private int mMinute = -1;

    private boolean mBtnCancelEnable;
    private boolean mBtnOKEnable;
    private boolean mShowDatePicker = true;

    // return true, to close DlgTimePicker; otherwise, return false;
    public interface btnHandler {
        boolean onBtnHandler();
    }

    private btnHandler mHandlerCancel;
    private btnHandler mHandlerOK;

    @Override
    public String getDialogTag() {
        return MainActivity.DLG_TIME_PICKER;
    }

    @Override
    public int getDialogTheme() {
        return R.style.app_dlg_scale_tt;
    }

    @Override
    public void saveData() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UtilDBG.logMethod();

        View view = inflater.inflate(R.layout.dlg_time_picker, container);

        mTextTitle = (TextView) view.findViewById(R.id.ynTitle);
        mDatePicker = (DatePicker) view.findViewById(R.id.datePicker);
        mTimePicker = (TimePicker) view.findViewById(R.id.timePicker);
        mBtnOK = (Button) view.findViewById(R.id.btnOK);
        mBtnCancel = (Button) view.findViewById(R.id.btnCancel);

        if (mStringTitle!=null) {
            mTextTitle.setText(mStringTitle);
        }
        if (mStringOK!=null) {
            mBtnOK.setText(mStringOK);
        }
        if (mStringCancel!=null) {
            mBtnCancel.setText(mStringCancel);
        }

        if (mYear != -1 && mMonth != -1 && mDay != -1) {
        	mDatePicker.updateDate(mYear, mMonth, mDay);
        }

        if (mHour != -1 && mMinute != -1) {
        	mTimePicker.setCurrentHour(mHour);
        	mTimePicker.setCurrentMinute(mMinute);
        }

        if (!mShowDatePicker) {
        	mDatePicker.setVisibility(View.GONE);
        }

        mBtnOK.setOnClickListener(this);
        mBtnOK.setEnabled(mBtnOKEnable);
        mBtnCancel.setOnClickListener(this);
        mBtnCancel.setEnabled(mBtnCancelEnable);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Ref. How to make DialogFragment width to ....
        // http://stackoverflow.com/questions/23990726/how-to-make-dialogfragment-width-to-fill-parent
        if (getDialog() != null && getDialog().getWindow()!=null) {
            getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }

    public void showDatePicker(boolean showDatePicker) {
        mShowDatePicker = showDatePicker;
        mBtnCancelEnable = true;
        mBtnOKEnable = true;
    }

    public DlgTimePicker setTitle(String title) {
        mStringTitle = title;
        return this;
    }

    // Date
    public DlgTimePicker setDate(int year, int month, int day) {
    	mYear = year;
    	mMonth = month;
    	mDay = day;
    	return this;
    }
    public int getCurrentYear() {
    	return mDatePicker.getYear();
    }
    public int getCurrentMonth() {
    	return mDatePicker.getMonth();
    }
    public int getCurrentDay() {
    	return mDatePicker.getDayOfMonth();
    }

    // Time
    public DlgTimePicker setTime(int hour, int minute) {
    	mHour = hour;
    	mMinute = minute;
    	return this;
    }
    public int getCurrentHour() {
    	return mTimePicker.getCurrentHour();
    }
    public int getCurrentMinute() {
    	return mTimePicker.getCurrentMinute();
    }

    public DlgTimePicker setPositiveButton(String text, btnHandler h) {
        if (text!=null) {
            mStringOK = text;
        }
        mHandlerOK = h;
        return this;
    }

    public DlgTimePicker setNegativeButton(String text, btnHandler h) {
        if (text!=null) {
            mStringCancel = text;
        }
        mHandlerCancel = h;
        return this;
    }

    public DlgTimePicker setButtonEnabled(boolean ok, boolean cancel) {
        mBtnOKEnable = ok;
        mBtnCancelEnable = ok;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v==mBtnOK) {
            if (mHandlerOK!=null) {
                if (mHandlerOK.onBtnHandler()) {
                    dismissIt();
                }
            } else {
                dismissIt();
            }
        } else if (v==mBtnCancel) {
            if (mHandlerCancel!=null) {
                if (mHandlerCancel.onBtnHandler()) {
                    dismissIt();
                }
            } else {
                dismissIt();
            }
        } else {
            UtilDBG.e("DlgTimePicker, unexpected onClick()");
        }
    }

    private void dismissIt() {
        if (getDialog() != null) {
            getDialog().dismiss();
        }
    }
}
