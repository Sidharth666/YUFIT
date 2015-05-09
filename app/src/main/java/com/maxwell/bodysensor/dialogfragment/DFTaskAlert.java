package com.maxwell.bodysensor.dialogfragment;

import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.maxwell.bodysensor.dialogfragment.dialog.DlgTimePicker;
import com.maxwell.bodysensor.listener.OnSetupDeviceAlertListener;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilLocale;
import com.maxwell.bodysensor.util.UtilLocale.DateFmt;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.SharedPrefWrapper;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgTimePicker.btnHandler;

public class DFTaskAlert extends DFBase {

	private MainActivity mActivity;
	private SharedPrefWrapper mSharedPref;

	private EditText mEdtTask1Title, mEdtTask2Title, mEdtTask3Title;
	private TextView mTxtTask1DateAndTime, mTxtTask2DateAndTime,
			mTxtTask3DateAndTime;
	private ImageButton mImgBtnDeleteTask1, mImgBtnDeleteTask2,
			mImgBtnDeleteTask3;
	private Button mBtnSetTask1DateAndTime, mBtnSetTask2DateAndTime,
			mBtnSetTask3DateAndTime;

	private String[] mTaskNames = new String[3];
	private long[] mTaskDateUnixTimes = new long[3];

    private OnSetupDeviceAlertListener mListener;
    public void setDeviceAlertListener(OnSetupDeviceAlertListener listener) {
        mListener = listener;
    }

	@Override
    public String getDialogTag() {
		return MainActivity.DF_TASK_ALERT;
	}

	@Override
    public int getDialogTheme() {
		return R.style.app_df_trans_rr;
	}

	@Override
    public void saveData() {
		for (int i = 0; i < 3; i++) {
			mSharedPref.setDeviceTaskAlert(i, mTaskNames[i], mTaskDateUnixTimes[i]);
		}

        if (mListener != null) {
            mListener.onDeviceAlertUpdated();
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		UtilDBG.logMethod();

		mActivity = (MainActivity) getActivity();
		mSharedPref = SharedPrefWrapper.getInstance();

		View view = inflater.inflate(R.layout.df_task_alert, container);

		mEdtTask1Title = (EditText) view.findViewById(R.id.edtTask1Title);
		mEdtTask2Title = (EditText) view.findViewById(R.id.edtTask2Title);
		mEdtTask3Title = (EditText) view.findViewById(R.id.edtTask3Title);

		mTxtTask1DateAndTime = (TextView) view
				.findViewById(R.id.txtTask1DateAndTime);
		mTxtTask2DateAndTime = (TextView) view
				.findViewById(R.id.txtTask2DateAndTime);
		mTxtTask3DateAndTime = (TextView) view
				.findViewById(R.id.txtTask3DateAndTime);

		// Show tasks' information.
		for (int i = 0; i < 3; i++) {
			mTaskNames[i] = mSharedPref.getDeviceTaskAlertName(i);
            mTaskDateUnixTimes[i] = mSharedPref.getDeviceTaskAlertUnixTime(i);
		}
		showTaskTitleDateAndTime(mEdtTask1Title, mTxtTask1DateAndTime, 0);
		showTaskTitleDateAndTime(mEdtTask2Title, mTxtTask2DateAndTime, 1);
		showTaskTitleDateAndTime(mEdtTask3Title, mTxtTask3DateAndTime, 2);

		mImgBtnDeleteTask1 = (ImageButton) view
				.findViewById(R.id.imgBtnDeleteTask1);
		mImgBtnDeleteTask1.setOnClickListener(imgBtnDeleteTaskOnClick);
		mImgBtnDeleteTask2 = (ImageButton) view
				.findViewById(R.id.imgBtnDeleteTask2);
		mImgBtnDeleteTask2.setOnClickListener(imgBtnDeleteTaskOnClick);
		mImgBtnDeleteTask3 = (ImageButton) view
				.findViewById(R.id.imgBtnDeleteTask3);
		mImgBtnDeleteTask3.setOnClickListener(imgBtnDeleteTaskOnClick);

		mBtnSetTask1DateAndTime = (Button) view
				.findViewById(R.id.btnSetTask1DateAndTime);
		mBtnSetTask1DateAndTime
				.setOnClickListener(btnSetTaskDateAndTimeOnClick);
		mBtnSetTask2DateAndTime = (Button) view
				.findViewById(R.id.btnSetTask2DateAndTime);
		mBtnSetTask2DateAndTime
				.setOnClickListener(btnSetTaskDateAndTimeOnClick);
		mBtnSetTask3DateAndTime = (Button) view
				.findViewById(R.id.btnSetTask3DateAndTime);
		mBtnSetTask3DateAndTime
				.setOnClickListener(btnSetTaskDateAndTimeOnClick);

        setupTitleText(view, R.string.df_task_alert_title);
		setupButtons(view);

		return view;
	}

	// iIndexTask = 0: the first task, 1: the second task, and so on.
	private void showTaskTitleDateAndTime(EditText edtTaskTitle,
			TextView txtTaskDateAndTime, int iIndexTask) {

		if (mTaskDateUnixTimes[iIndexTask] == 0) { // The task is empty.
			if (edtTaskTitle != null)
				edtTaskTitle.setText("");

			if (txtTaskDateAndTime != null)
				txtTaskDateAndTime.setText(R.string.df_task_date_time_hint);

			return;
		}

		// Show task's title.
		if (edtTaskTitle != null)
			edtTaskTitle.setText(mTaskNames[iIndexTask]);

		if (txtTaskDateAndTime != null) {
			UtilCalendar cal = new UtilCalendar(mTaskDateUnixTimes[iIndexTask],
					null);
			Date date = new Date(cal.getTimeInMillis());
			txtTaskDateAndTime.setText(UtilLocale.dateToString(date,
					DateFmt.YMDHMa));
		}
	}

	private void showDateAndTimePickerDlg(final int iIndexTask) {
		long dateUnixTime = mTaskDateUnixTimes[iIndexTask];
		UtilCalendar cal;
		if (dateUnixTime == 0) {
			cal = new UtilCalendar(null);
		} else {
			cal = new UtilCalendar(dateUnixTime, null);
		}

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);

		final DlgTimePicker dlg = new DlgTimePicker();
        dlg.showDatePicker(true);
		dlg.setDate(year, month, day).setTime(hour, minute)
				.setPositiveButton(null, new btnHandler() {

					@Override
					public boolean onBtnHandler() {
						int year = dlg.getCurrentYear();
						int month = dlg.getCurrentMonth() + 1;
						int day = dlg.getCurrentDay();
						int hour = dlg.getCurrentHour();
						int minute = dlg.getCurrentMinute();

						UtilCalendar cal = new UtilCalendar(year, month, day,
								hour, minute, 0, null);
                        mTaskDateUnixTimes[iIndexTask] = cal.getUnixTime();

						switch (iIndexTask) {
						case 0:
							mTaskNames[0] = mEdtTask1Title.getEditableText()
									.toString();
							showTaskTitleDateAndTime(mEdtTask1Title,
									mTxtTask1DateAndTime, 0);
							break;
						case 1:
							mTaskNames[1] = mEdtTask2Title.getEditableText()
									.toString();
							showTaskTitleDateAndTime(mEdtTask2Title,
									mTxtTask2DateAndTime, 1);
							break;
						case 2:
							mTaskNames[2] = mEdtTask3Title.getEditableText()
									.toString();
							showTaskTitleDateAndTime(mEdtTask3Title,
									mTxtTask3DateAndTime, 2);
							break;
						}

						return true;
					}

				});

		dlg.showHelper(mActivity);
	}

	private View.OnClickListener imgBtnDeleteTaskOnClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == mImgBtnDeleteTask1) {
				mTaskNames[0] = "";
                mTaskDateUnixTimes[0] = 0;
				mEdtTask1Title.setText("");
				mTxtTask1DateAndTime.setText(R.string.df_task_date_time_hint);
			} else if (v == mImgBtnDeleteTask2) {
				mTaskNames[1] = "";
                mTaskDateUnixTimes[1] = 0;
				mEdtTask2Title.setText("");
				mTxtTask2DateAndTime.setText(R.string.df_task_date_time_hint);
			} else if (v == mImgBtnDeleteTask3) {
				mTaskNames[2] = "";
                mTaskDateUnixTimes[2] = 0;
				mEdtTask3Title.setText("");
				mTxtTask3DateAndTime.setText(R.string.df_task_date_time_hint);
			}
		}
	};

	private View.OnClickListener btnSetTaskDateAndTimeOnClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == mBtnSetTask1DateAndTime)
				showDateAndTimePickerDlg(0);
			else if (v == mBtnSetTask2DateAndTime)
				showDateAndTimePickerDlg(1);
			else if (v == mBtnSetTask3DateAndTime)
				showDateAndTimePickerDlg(2);
		}
	};

}
