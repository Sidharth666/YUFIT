package com.maxwell.bodysensor.dialogfragment;

import java.util.Calendar;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.maxwell.bodysensor.dialogfragment.dialog.DlgMessageOK;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilLocale;
import com.maxwell.bodysensor.util.UtilLocale.DateFmt;
import com.maxwell.bodysensor.util.UtilTZ;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.DBProgramData;

public class DFLogSleepEntry extends DFBase {
    DBProgramData mPD;

    private TextView mTextHintBegin;
    private TextView mTextHintEnd;
    private TextView mTextBegin;
    private TextView mTextEnd;

    private View mViewPickeArea;
    private NumberPicker mNP;
    private TimePicker mTP;

    private UtilCalendar mCalBegin;
    private UtilCalendar mCalEnd;
    private UtilCalendar mCalToday;
    private UtilCalendar mCalYesterday;

    private static final int MODE_NONE = 0;
    private static final int MODE_BEGIN = 1;
    private static final int MODE_END = 2;
    private int mSetMode = MODE_NONE;

    @Override
    public String getDialogTag() {
        return MainActivity.DF_LOG_SLEEP_ENTRY;
    }

    @Override
    public int getDialogTheme() {
        return R.style.app_df_trans_rr;
    }

    @Override
    public void saveData() {
        String address = mPD.getTargetDeviceMac();
        if (address.compareToIgnoreCase("")!=0) {
            mPD.addSleepLog(mCalToday, mCalBegin, mCalEnd, address);

            FragmentActivity a = getActivity();
            if (a instanceof MainActivity) {
                List<Fragment> fList = ((MainActivity)a).getFragmentList(MainActivity.TAB_SPEC_TREND, true);
                if (fList.size()>0) {
                    Fragment f = fList.get(0);
                    /*if (f instanceof FTabTrend) {
                        ((FTabTrend) f).addAlarmData();
                    }*/
                }
            }
        }
    }

    @Override
    public boolean checkData() {
        UtilCalendar calNow = new UtilCalendar(UtilTZ.getDefaultTZ());
        boolean bCheck1 = mCalBegin.before(mCalEnd);
        boolean bCheck2 = mCalEnd.before(calNow);
        boolean bCheck3 = (mCalEnd.getDiffSeconds(mCalBegin) >= 60*60);

        if (bCheck1 && bCheck2 && bCheck3)
            return true;

        DlgMessageOK dlg = new DlgMessageOK();
        if (!bCheck1) {
            dlg.addDesString(getResources().getString(R.string.sl_error_begin_end));
        }
        if (!bCheck2) {
            dlg.addDesString(getResources().getString(R.string.sl_error_end_now));
        }
        if (bCheck1 && !bCheck3) {
            // because, if bCheck1 is false, bCheck3 is guaranteed to be false, do not need to show again
            dlg.addDesString(getResources().getString(R.string.sl_error_sixty_minutes));
        }
        dlg.showHelper(getActivity());

        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UtilDBG.logMethod();

        View view = inflater.inflate(R.layout.df_log_sleep_entry, container);

        mPD = DBProgramData.getInstance();

        mCalEnd = new UtilCalendar(null);
        mCalEnd.set(Calendar.HOUR_OF_DAY, 7);
        mCalEnd.set(Calendar.MINUTE, 0);
        mCalEnd.set(Calendar.SECOND, 0);
        mCalEnd.set(Calendar.MILLISECOND, 0);
        mCalBegin = (UtilCalendar) mCalEnd.clone();
        mCalBegin.add(Calendar.HOUR_OF_DAY, -8);
        mCalToday = mCalEnd.getFirstSecondBDay();
        mCalYesterday = mCalBegin.getFirstSecondBDay();

        mViewPickeArea = view.findViewById(R.id.picker_area);

        mNP = (NumberPicker) view.findViewById(R.id.slNumPicker);
        mNP.setMinValue(0);
        mNP.setMaxValue(1);
        String [] display = new String[2];
        display[0] = UtilLocale.calToString(mCalYesterday, DateFmt.MD);
        display[1] = UtilLocale.calToString(mCalToday, DateFmt.MD);
        mNP.setDisplayedValues(display);

        mTP = (TimePicker) view.findViewById(R.id.slTimePicker);

        mTextHintBegin = (TextView) view.findViewById(R.id.sl_hint_begin);
        mTextHintEnd = (TextView) view.findViewById(R.id.sl_hint_end);
        mTextBegin = (TextView) view.findViewById(R.id.slBegin);
        mTextEnd = (TextView) view.findViewById(R.id.slEnd);
        mTextBegin.setOnClickListener(mShowSetTime);
        mTextEnd.setOnClickListener(mShowSetTime);
        mTextBegin.setText(UtilLocale.calToString(mCalBegin, DateFmt.YMDHMa));
        mTextEnd.setText(UtilLocale.calToString(mCalEnd, DateFmt.YMDHMa));

        setupPicker(mCalBegin);

        mNP.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker arg0, int arg1, int arg2) {
                onValueChanged();
            }
        });
        mTP.setOnTimeChangedListener(new OnTimeChangedListener() {

            @Override
            public void onTimeChanged(TimePicker arg0, int arg1, int arg2) {
                onValueChanged();
            }
        });

        setMode(MODE_NONE);

        setupTitleText(view, R.string.sl_sleep_entry);
        setupButtons(view);

        return view;
    }

    OnClickListener mShowSetTime = new OnClickListener() {
        @Override
        public void onClick(View v) {
            UtilDBG.logMethod();
            if (v==mTextBegin) {
                setMode(MODE_BEGIN);
            } else if (v==mTextEnd) {
                setMode(MODE_END);
            }
        }
    };

    private void setMode(int i) {
        mSetMode = i;
        mTextHintBegin.setActivated(mSetMode==MODE_BEGIN);
        mTextHintEnd.setActivated(mSetMode==MODE_END);
        mTextBegin.setActivated(mSetMode==MODE_BEGIN);
        mTextEnd.setActivated(mSetMode==MODE_END);

        if (mSetMode==MODE_BEGIN || mSetMode==MODE_END) {
            mViewPickeArea.setVisibility(View.VISIBLE);
        } else {
            mViewPickeArea.setVisibility(View.INVISIBLE);
        }

        if (mSetMode==MODE_BEGIN) {
            setupPicker(mCalBegin);
        } else if (mSetMode==MODE_END) {
            setupPicker(mCalEnd);
        }
    }

    private void setupPicker(UtilCalendar cal) {
        int iNumIndex = 1;
        if (cal.isSameDate(mCalYesterday)) {
            iNumIndex = 0;
        } else if (cal.isSameDate(mCalToday)) {
            iNumIndex = 1;
        } else {
            UtilDBG.e("unexpected in setupPicker()");
        }

        mNP.setValue(iNumIndex);

        mTP.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        mTP.setCurrentMinute(cal.get(Calendar.MINUTE));
    }

    private UtilCalendar getTimeFromPicker() {
        UtilCalendar calRef = mCalToday;
        int iNumIndex = mNP.getValue();
        if (iNumIndex == 0) {
            calRef = mCalYesterday;
        } else if (iNumIndex == 1) {
            calRef = mCalToday;
        } else {
            UtilDBG.e("unexpected in getTimeFromPicker");
        }

        UtilCalendar cal = new UtilCalendar(
                calRef.get(Calendar.YEAR),
                calRef.get(Calendar.MONTH)+1,
                calRef.get(Calendar.DAY_OF_MONTH),
                mTP.getCurrentHour(),
                mTP.getCurrentMinute(),
                0,
                UtilTZ.getDefaultTZ()
                );

        return cal;
    }

    void onValueChanged() {
        if (mSetMode==MODE_BEGIN) {
            mCalBegin = getTimeFromPicker();
            mTextBegin.setText(UtilLocale.calToString(mCalBegin, DateFmt.YMDHMa));
        }else if (mSetMode==MODE_END) {
            mCalEnd = getTimeFromPicker();
            mTextEnd.setText(UtilLocale.calToString(mCalEnd, DateFmt.YMDHMa));
        }
    }
}
