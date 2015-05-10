package com.maxwell.bodysensor.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.maxwell.bodysensor.CameraActivity;
import com.maxwell.bodysensor.MXWApp;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.SharedPrefWrapper;
import com.maxwell.bodysensor.data.DBDevice;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.user.DBUserDevice;
import com.maxwell.bodysensor.dialogfragment.DFAbout;
import com.maxwell.bodysensor.dialogfragment.DFAddNewDevice;
import com.maxwell.bodysensor.dialogfragment.DFBase;
import com.maxwell.bodysensor.dialogfragment.DFDeviceAlarm;
import com.maxwell.bodysensor.dialogfragment.DFDeviceList;
import com.maxwell.bodysensor.dialogfragment.DFEmergencyContact;
import com.maxwell.bodysensor.dialogfragment.DFInfo;
import com.maxwell.bodysensor.dialogfragment.DFMoveAlert;
import com.maxwell.bodysensor.dialogfragment.DFOutOfRangeAlert;
import com.maxwell.bodysensor.dialogfragment.DFPhoneNotify;
import com.maxwell.bodysensor.dialogfragment.DFSOSHistory;
import com.maxwell.bodysensor.dialogfragment.DFTaskAlert;
import com.maxwell.bodysensor.dialogfragment.DFTutorial;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgMessageYN;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgMessageYN.btnHandler;
import com.maxwell.bodysensor.listener.OnSetupDeviceAlertListener;
import com.maxwell.bodysensor.listener.OnSetupOutOfRangeListener;
import com.maxwell.bodysensor.listener.OnSyncDeviceListener;
import com.maxwell.bodysensor.ui.WarningUtil;
import com.maxwell.bodysensor.util.UtilCVT;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilLocale;
import com.maxwell.bodysensor.util.UtilLocale.DateFmt;
import com.maxwell.bodysensor.util.UtilTime;
import com.maxwellguider.bluetooth.MGPeripheral;
import com.maxwellguider.bluetooth.MGPeripheral.DeviceType;
import com.maxwellguider.bluetooth.activitytracker.AlertTime;
import com.maxwellguider.bluetooth.activitytracker.LanguageType;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;
import com.maxwellguider.bluetooth.activitytracker.UnitType;

import java.util.Date;

/**
 * The fragment, Conf. (Setting)
 */

public class FTabConf extends Fragment implements
        View.OnClickListener, DBDevice.OnDBDeviceUpdateListener,
        DFPhoneNotify.OnPhoneNotifyChangedLitener,
        OnSetupOutOfRangeListener,
        OnSetupDeviceAlertListener,OnSyncDeviceListener {

    public static final int BATTERY_CHARGING = 101;
    public static final int BATTERY_FULL = 102;

    private MainActivity mActivity;
    private DBProgramData mPD;
    private SharedPrefWrapper mSharedPref;
    private MGActivityTrackerApi mMaxwellBLE;
    private DBDevice mDevManager;

    private View mViewFocusE2MAX;
    private View mViewGeneal;

    private TextView mTextBatteryLevel;

    private View mViewPairADevice;
    private View mViewDeviceList;

    private View mViewTutorial;
    private View mViewAbout;

	// Advanced device feature
	private View mViewPhoneNotify;
	private View mViewOutOfRange;
    private View mViewFineDevice;
	private View mViewSmartKey;
    private View mViewCameraRemote;
    private View mViewVideoRemote;
    private View mViewFindPhone;
	private View mViewDeviceAlarm;
	private View mViewTaskAlert;
	private View mViewMoveAlert;
	private View mViewResetDevice;
    private View mViewEmergencyContact;
    private View mViewSOSHistory;

    private Button mBtnFindDevice;
    private Button mBtnLaunchCamera;
    private Button mBtnLaunchVideo;

	private ToggleButton mTogglePhoneNotify;
	private ToggleButton mToggleOutOfRange;
    private ToggleButton mToggleFindPhone;
	private ToggleButton mToggleMoveAlert;
    private ToggleButton mToggleSOS;
	private ToggleButton mToggleVibration;

    private TextView mTextNoDisturbingTime;
    private TextView mTextOutOfRnageNoDisturbingTime;
    private TextView mTextDeviceAlarmDetail;
    private TextView mTextMoveAlertDuration;

    private ProgressBar mSyncProgress;
    private TextView mTextConnectionStat;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        UtilDBG.logMethod();

		View rootView = inflater.inflate(R.layout.fragment_conf, container, false);

		mActivity = (MainActivity) getActivity();
        mPD = DBProgramData.getInstance();
        mSharedPref = SharedPrefWrapper.getInstance();
        mMaxwellBLE = MGActivityTracker.getInstance(mActivity);

        mDevManager = DBUserDevice.getInstance();
        mDevManager.addListener(this);

        mActivity.setOnSyncDeviceListener(this);

        mViewFocusE2MAX = rootView.findViewById(R.id.view_device_advanced_functions);
        mViewGeneal = rootView.findViewById(R.id.view_device_general_functions);
        mTextBatteryLevel = (TextView) rootView.findViewById(R.id.text_battery_level);
        mViewPairADevice = rootView.findViewById(R.id.add_a_device);
        mViewDeviceList = rootView.findViewById(R.id.view_device_list);
        mViewTutorial = rootView.findViewById(R.id.view_tutirial);
        mViewAbout = rootView.findViewById(R.id.view_about);

        //++ Advanced device feature
        mViewPhoneNotify = rootView.findViewById(R.id.view_phone_notify);
        mViewOutOfRange = rootView.findViewById(R.id.view_out_of_range);
        mViewFineDevice = rootView.findViewById(R.id.view_find_device);
        mViewSmartKey = rootView.findViewById(R.id.view_smart_key);
        mViewCameraRemote = rootView.findViewById(R.id.view_camera_remote);
        mViewVideoRemote = rootView.findViewById(R.id.view_video_remote);
        mViewFindPhone = rootView.findViewById(R.id.view_find_phone);
        mViewDeviceAlarm = rootView.findViewById(R.id.view_device_alarm);
        mViewTaskAlert = rootView.findViewById(R.id.view_task_alert);
        mViewMoveAlert = rootView.findViewById(R.id.view_move_alert);
        mViewResetDevice = rootView.findViewById(R.id.view_reset_device);
        mViewEmergencyContact = rootView.findViewById(R.id.view_emergency_contact);
        mViewSOSHistory = rootView.findViewById(R.id.view_sos_history);

        mBtnFindDevice = (Button) rootView.findViewById(R.id.btn_find_device);
        mBtnLaunchCamera = (Button) rootView.findViewById(R.id.btn_launch_camera);
        mBtnLaunchVideo = (Button) rootView.findViewById(R.id.btn_launch_video);


//    	mRGUnit = (RadioGroup) rootView.findViewById(R.id.radio_unit);

    	mToggleVibration = (ToggleButton) rootView.findViewById(R.id.toggle_vibration);
    	mTogglePhoneNotify = (ToggleButton) rootView.findViewById(R.id.toggle_phone_notify);
        mToggleFindPhone = (ToggleButton) rootView.findViewById(R.id.toggle_find_phone);
    	mToggleOutOfRange = (ToggleButton) rootView.findViewById(R.id.toggle_out_of_range);
    	mToggleMoveAlert = (ToggleButton) rootView.findViewById(R.id.toggle_move_alert);
        mToggleSOS = (ToggleButton) rootView.findViewById(R.id.toggle_sos);

        mTextNoDisturbingTime = (TextView) rootView.findViewById(R.id.text_no_disturbing_time);
        mTextOutOfRnageNoDisturbingTime = (TextView) rootView.findViewById(R.id.text_out_of_range_no_disturbing_time);
        mTextDeviceAlarmDetail = (TextView) rootView.findViewById(R.id.text_device_alarm_detail);
        mTextMoveAlertDuration = (TextView) rootView.findViewById(R.id.text_move_alert_duration);

        mViewPairADevice.setOnClickListener(this);
        mViewDeviceList.setOnClickListener(this);
        mViewTutorial.setOnClickListener(this);
        mViewAbout.setOnClickListener(this);

        mViewPhoneNotify.setOnClickListener(this);
        mViewOutOfRange.setOnClickListener(this);
        mViewFineDevice.setOnClickListener(this);
        mViewCameraRemote.setOnClickListener(this);
        mViewVideoRemote.setOnClickListener(this);
        mViewFindPhone.setOnClickListener(this);
        mViewDeviceAlarm.setOnClickListener(this);
        mViewTaskAlert.setOnClickListener(this);
        mViewMoveAlert.setOnClickListener(this);
        mViewResetDevice.setOnClickListener(this);
        mViewEmergencyContact.setOnClickListener(this);
        mViewSOSHistory.setOnClickListener(this);

        mBtnFindDevice.setOnClickListener(this);
        mBtnLaunchCamera.setOnClickListener(this);
        mBtnLaunchVideo.setOnClickListener(this);

        updateView();

	    return rootView;
	}

    @Override
    public void onResume() {
        super.onResume();

        // For BLE Devie
        onIncomingCallSave();
        onOutOfRangeEnableChanged();

        updateWeeklyAlarmView();
        updateMoveAlertView();

        mTogglePhoneNotify.setChecked(mSharedPref.isDeviceIncomingCallEnable());
        mTogglePhoneNotify.setOnCheckedChangeListener(mPNSwitchListener);

    	mToggleOutOfRange.setChecked(mSharedPref.isDeviceOutOfRangeEnable());
    	mToggleOutOfRange.setOnCheckedChangeListener(mOutOfRangeSwitchListener);

    	mToggleMoveAlert.setChecked(mSharedPref.isDeviceMoveAlertEnable());
    	mToggleMoveAlert.setOnCheckedChangeListener(mMoveAlertSwitchListener);

        mToggleSOS.setChecked(mSharedPref.isDeviceSOSEnable());
        mToggleSOS.setOnCheckedChangeListener(mSOSSwitchListener);

        mToggleVibration.setChecked(mSharedPref.isDeviceVibrationEnable());
        mToggleVibration.setOnCheckedChangeListener(mVibrateSwitchListener);

        mToggleFindPhone.setChecked(mSharedPref.isFindPhoneEnable());
        mToggleFindPhone.setOnCheckedChangeListener(mFindPhoneSwitchListener);

        boolean enablePhoneConnection = mSharedPref.isPhoneConnectionEnable();
        enablePhoneConnectionUpdated(enablePhoneConnection);
    }

    @Override
    public void onPause() {
        super.onPause();

        mTogglePhoneNotify.setOnCheckedChangeListener(null);
    	mToggleOutOfRange.setOnCheckedChangeListener(null);
        mToggleMoveAlert.setOnCheckedChangeListener(null);
        mToggleSOS.setOnCheckedChangeListener(null);
        mToggleVibration.setOnCheckedChangeListener(null);
        mToggleFindPhone.setOnCheckedChangeListener(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mDevManager.removeListener(this);
    }

    private void enablePhoneConnectionUpdated(boolean enable) {
        mViewPhoneNotify.setEnabled(enable);
        mViewOutOfRange.setEnabled(enable);
        mViewFineDevice.setEnabled(enable);
        mViewSmartKey.setEnabled(enable);

        mTogglePhoneNotify.setClickable(enable);
        mToggleOutOfRange.setClickable(enable);
        mToggleFindPhone.setClickable(enable);
        mViewFineDevice.setClickable(enable);

        if (enable) {
            mTogglePhoneNotify.setChecked(mSharedPref.isDeviceIncomingCallEnable());
            mTogglePhoneNotify.setOnCheckedChangeListener(mPNSwitchListener);

            mToggleOutOfRange.setChecked(mSharedPref.isDeviceOutOfRangeEnable());
            mToggleOutOfRange.setOnCheckedChangeListener(mOutOfRangeSwitchListener);

            mToggleFindPhone.setChecked(mSharedPref.isFindPhoneEnable());
            mToggleFindPhone.setOnCheckedChangeListener(mFindPhoneSwitchListener);

            mBtnFindDevice.setOnClickListener(this);
            mBtnLaunchCamera.setOnClickListener(this);
            mBtnLaunchVideo.setOnClickListener(this);
        } else {
            mTogglePhoneNotify.setOnCheckedChangeListener(null);
            mTogglePhoneNotify.setChecked(false);

            mToggleOutOfRange.setOnCheckedChangeListener(null);
            mToggleOutOfRange.setChecked(false);

            mToggleFindPhone.setOnCheckedChangeListener(null);
            mToggleFindPhone.setChecked(false);

            mBtnFindDevice.setOnClickListener(null);
            mBtnLaunchCamera.setOnClickListener(null);
            mBtnLaunchVideo.setOnClickListener(null);
        }
    }

    public void updateView() {
        String TargetMac = mPD.getTargetDeviceMac();
        boolean hasTargetDevice = (UtilCVT.getMacAddressType(TargetMac) == 2) && (mPD.getUserDeviceByAddress(TargetMac)!=null);


        // TODO : check with PM
        mViewGeneal.setVisibility(hasTargetDevice ? View.VISIBLE : View.GONE);
        if (hasTargetDevice) {
//            DeviceData device = mPD.getDeviceDataByAddress(TargetMac);
            DeviceType type = mMaxwellBLE.getDeviceType(TargetMac);
            if (type == DeviceType.ENERGY_CAPSULE) {
                mViewFocusE2MAX.setVisibility(View.GONE);
            } else {
                mViewFocusE2MAX.setVisibility(View.VISIBLE);
            }
        } else {
            mViewFocusE2MAX.setVisibility(View.GONE);
        }

        if (hasTargetDevice) {
            int battery = mSharedPref.getTargetDeviceBattery();

            String strBattery = "";
            if (battery == BATTERY_CHARGING) {
                strBattery = getString(R.string.bt_device_battery_charging);
            } else if (battery == BATTERY_FULL) {
                strBattery = getString(R.string.bt_device_battery_fully_charged);
            } else {
                int iShow = (battery/10) * 10;
                // TODO : Why?
//            int iShow = (int)((dOriginal-45.0)/(100.0-45.0)*10.0+0.5) * 10;
//            if (iShow < 0) {
//                iShow = 0;
//            } else if (iShow > 100) {
//                iShow = 100;
//            }

                strBattery = Integer.toString(iShow) + "%";
            }

            mTextBatteryLevel.setText(strBattery);
        }
    }

    private void showDlgDeviceNotConnected() {
		WarningUtil.showDFMessageOK(getActivity(), 0, R.string.dlg_device_disconnectd_content);
	}

    private void updateTextTimeDuration(TextView tv, int start, int end) {
        long msStart = UtilTime.getMillisForIntTime(start);
        long msEnd = UtilTime.getMillisForIntTime(end);

        String strStart = UtilLocale.dateToString(new Date(msStart), DateFmt.HMa);
        String strEnd = UtilLocale.dateToString(new Date(msEnd), DateFmt.HMa);

        StringBuilder sb = new StringBuilder().append(strStart).append(" - ").append(strEnd);
        tv.setText(sb.toString());
    }

    // time format  ex: ( 20:30 => 1230 )
    private AlertTime getAlertTime(int time) {
        AlertTime alarmTime = new AlertTime();
        alarmTime.hour = time / 60;
        alarmTime.minute = time % 60;

        return alarmTime;
    }

    private void updateWeeklyAlarmView() {
        int weeklyAlarmMask = mSharedPref.getDeviceWeeklyAlarmMask();
        int time = mSharedPref.getDeviceWeeklyAlarmTime();

        long msTime = UtilTime.getMillisForIntTime(time);

        String strTime = UtilLocale.dateToString(new Date(msTime), DateFmt.HMa);
        StringBuilder sb = new StringBuilder().append(strTime);

        // Check for Sunday.
        String strWeek = "";
        if ((weeklyAlarmMask & DFDeviceAlarm.WEEKLY_ALARM.SUNDAY.getValue())
                == DFDeviceAlarm.WEEKLY_ALARM.SUNDAY.getValue())
            strWeek += " " + getString(R.string.strShortSunday);

        // Check for Monday.
        if ((weeklyAlarmMask & DFDeviceAlarm.WEEKLY_ALARM.MONDAY.getValue())
                == DFDeviceAlarm.WEEKLY_ALARM.MONDAY.getValue())
            strWeek += " " + getString(R.string.strShortMonday);

        // Check for Tuesday.
        if ((weeklyAlarmMask & DFDeviceAlarm.WEEKLY_ALARM.TUESDAY.getValue())
                == DFDeviceAlarm.WEEKLY_ALARM.TUESDAY.getValue())
            strWeek += " " + getString(R.string.strShortTuesday);

        // Check for Wednesday.
        if ((weeklyAlarmMask & DFDeviceAlarm.WEEKLY_ALARM.WEDNESDAY.getValue())
                == DFDeviceAlarm.WEEKLY_ALARM.WEDNESDAY.getValue())
            strWeek += " " + getString(R.string.strShortWednesday);

        // Check for Thursday.
        if ((weeklyAlarmMask & DFDeviceAlarm.WEEKLY_ALARM.THURSDAY.getValue())
                == DFDeviceAlarm.WEEKLY_ALARM.THURSDAY.getValue())
            strWeek += " " + getString(R.string.strShortThursday);

        // Check for Friday.
        if ((weeklyAlarmMask & DFDeviceAlarm.WEEKLY_ALARM.FRIDAY.getValue())
                == DFDeviceAlarm.WEEKLY_ALARM.FRIDAY.getValue())
            strWeek += " " + getString(R.string.strShortFriday);

        // Check for Saturday.
        if ((weeklyAlarmMask & DFDeviceAlarm.WEEKLY_ALARM.SATURDAY.getValue())
                == DFDeviceAlarm.WEEKLY_ALARM.SATURDAY.getValue())
            strWeek += " " + getString(R.string.strShortSaturday);

        if (strWeek.length() > 0) {
            sb.append(",").append(strWeek);
        }

        mTextDeviceAlarmDetail.setText(sb);
    }

    private void updateMoveAlertView() {
        mToggleMoveAlert.setChecked(mSharedPref.isDeviceMoveAlertEnable());

        int start = mSharedPref.getDeviceMoveAlertStart();
        int end = mSharedPref.getDeviceMoveAlertEnd();

        updateTextTimeDuration(mTextMoveAlertDuration, start, end);
    }

    private void configSystemSetting() {
        if (mMaxwellBLE.isReady()) {
            UnitType unitType = mSharedPref.getProfileUnit();
            LanguageType language = mSharedPref.getDeviceLanguage();
            boolean vibration = mSharedPref.isDeviceVibrationEnable();
            mMaxwellBLE.configSystemSetting(unitType, language, vibration);
        }
    }

    private void configAlertSetting() {
        if (mMaxwellBLE.isReady()) {
            UtilCalendar cal;

            // Weekly Alarm
            int weeklyAlarmMask = mSharedPref.getDeviceWeeklyAlarmMask();
            AlertTime weeklyAlarmTime = getAlertTime(mSharedPref.getDeviceWeeklyAlarmTime());

            // Task Alert
            cal = new UtilCalendar(mSharedPref.getDeviceTaskAlertUnixTime(0), null);
            Date task1Date = new Date(cal.getTimeInMillis());

            cal = new UtilCalendar(mSharedPref.getDeviceTaskAlertUnixTime(1), null);
            Date task2Date = new Date(cal.getTimeInMillis());

            cal = new UtilCalendar(mSharedPref.getDeviceTaskAlertUnixTime(2), null);
            Date task3Date = new Date(cal.getTimeInMillis());

            // Move Alert
            boolean enableMoveAlert = mSharedPref.isDeviceMoveAlertEnable();
            AlertTime moveAlertTimeStart = getAlertTime(mSharedPref.getDeviceMoveAlertStart());
            AlertTime moveAlertTimeENd = getAlertTime(mSharedPref.getDeviceMoveAlertEnd());

            mMaxwellBLE.configAlertSetting(
                    weeklyAlarmMask, weeklyAlarmTime,                       // weekly alarm
                    task1Date, task2Date, task3Date,                        // task alert
                    enableMoveAlert, moveAlertTimeStart, moveAlertTimeENd); // move alert
        }
    }

    private void setLinkLossIndicator(boolean enable) {
        if (mMaxwellBLE.isReady()) {
            mMaxwellBLE.setLinkLossIndicator(enable);
        }
    }

    @Override
    public void onClick(View v) {
        if (MXWApp.isClickFast(v)) {
            return;
        }

        DFBase dlg = null;
        String strURL = "";

        if (v==null) {
        }  else if (v==mViewPairADevice) {
            dlg = new DFAddNewDevice();
        } else if (v==mViewDeviceList) {
            dlg = new DFDeviceList();
        }  else if (v==mViewPhoneNotify) {
        	dlg = new DFPhoneNotify();
            ((DFPhoneNotify) dlg).setOnPhoneNotifyChangedLitener(this);
        } else if (v==mViewOutOfRange) {
            dlg = new DFOutOfRangeAlert();
            ((DFOutOfRangeAlert) dlg).setOutOfRangeListener(this);
        } else if (v==mViewFineDevice) {
            dlg = new DFInfo();
            ((DFInfo) dlg).setTitleResId(R.string.df_smart_keys_find_device);
            ((DFInfo) dlg).setInfo(getString(R.string.find_device_des));
        } else if (v==mViewCameraRemote) {
            dlg = new DFInfo();
            ((DFInfo) dlg).setTitleResId(R.string.df_smart_keys_camera_remote);
            ((DFInfo) dlg).setInfo(getString(R.string.df_smart_keys_instruction_remote_camera));
        } else if (v==mViewVideoRemote) {
            dlg = new DFInfo();
            ((DFInfo) dlg).setTitleResId(R.string.df_smart_keys_video_remote);
            ((DFInfo) dlg).setInfo(getString(R.string.df_smart_keys_instruction_remote_video));
        } else if (v==mViewFindPhone) {
            dlg = new DFInfo();
            ((DFInfo) dlg).setTitleResId(R.string.df_smart_keys_find_phone);
            ((DFInfo) dlg).setInfo(getString(R.string.df_smart_keys_instruction_find_phone));
        } else if (v==mViewDeviceAlarm) {
        	dlg = new DFDeviceAlarm();
        	((DFDeviceAlarm) dlg).setDeviceAlertListener(this);
        } else if (v==mViewTaskAlert) {
            dlg = new DFTaskAlert();
            ((DFTaskAlert) dlg).setDeviceAlertListener(this);
        } else if (v==mViewMoveAlert) {
            dlg = new DFMoveAlert();
            ((DFMoveAlert) dlg).setDeviceAlertListener(this);
        } else if (v==mViewEmergencyContact) {
            dlg = new DFEmergencyContact();
        } else if (v==mViewSOSHistory) {
            dlg = new DFSOSHistory();
        } else if (v==mViewResetDevice) {
        	if (mMaxwellBLE.isReady()) {
        		dlg = new DlgMessageYN();
                ((DlgMessageYN)dlg).setTitle(getString(R.string.fcResetDevice))
        		        .setDes(getString(R.string.ynResetDeviceDes))
                        .setPositiveButton(null, new btnHandler() {

        			@Override
        			public boolean onBtnHandler() {
                        mMaxwellBLE.resetDevice();
        				return true;
        			}

            	});
            } else {
            	showDlgDeviceNotConnected();
            	return;
            }
        } else if (v==mBtnFindDevice) {
            if (mMaxwellBLE.isReady()) {
                mMaxwellBLE.findDevice();
            } else {
                showDlgDeviceNotConnected();
            }
            return;
        } else if (v==mBtnLaunchCamera) {
            Intent intent = new Intent(mActivity, CameraActivity.class);
            intent.setAction(CameraActivity.ACTION_TAKE_PICTURE);
            startActivity(intent);
        } else if (v==mBtnLaunchVideo) {
            Intent intent = new Intent(mActivity, CameraActivity.class);
            intent.setAction(CameraActivity.ACTION_VIDEO_RECORDER);
            startActivity(intent);
        } else if (v==mViewTutorial) {
            dlg = new DFTutorial();
        } else if (v==mViewAbout) {
            dlg = new DFAbout();
        }

//        if (strURL.length()>0) {
//            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(strURL));
//            startActivity(browserIntent);
//            return;
//        }

        if (dlg!=null) {
            dlg.showHelper(FTabConf.this.getActivity());
            return;
        }

        UtilDBG.e("FConf, onclick, unexpected, no action");
    }

    @Override
    public void OnDBDeviceUpdated() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                updateView();
            }
        });
    }

    @Override
    public void onIncomingCallSave() {
        boolean enable = mSharedPref.isInComingCallNoDisturbingEnable();
        if (!enable) {
        } else {
            int start = mSharedPref.getInComingCallNoDisturbingStart();
            int end = mSharedPref.getInComingCallNoDisturbingEnd();

            updateTextTimeDuration(mTextNoDisturbingTime, start, end);
        }
    }

    @Override
    public void onOutOfRangeEnableChanged() {
        boolean enable = mSharedPref.isOutOfRangeNoDisturbingEnable();
        if (!enable) {
        } else {
            int start = mSharedPref.getOutOfRangeNoDisturbingStart();
            int end = mSharedPref.getOutOfRangeNoDisturbingEnd();

            updateTextTimeDuration(mTextOutOfRnageNoDisturbingTime, start, end);
        }
    }

    @Override
    public void onDeviceAlertUpdated() {
        updateWeeklyAlarmView();
        updateMoveAlertView();

        configAlertSetting();
    }


    private CompoundButton.OnCheckedChangeListener mFindPhoneSwitchListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {

                    mSharedPref.enableFindPhone(isChecked);
                }
            };

    private CompoundButton.OnCheckedChangeListener mPhoneConnectionSwitchListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {

                    mSharedPref.enablePhoneConnection(isChecked);
                    enablePhoneConnectionUpdated(isChecked);

                    mMaxwellBLE.setAutoConnect(isChecked);
                    if (isChecked) {
                        // connect
                        if (!mMaxwellBLE.isConnected()) {
                            mMaxwellBLE.connect(mPD.getTargetDeviceMac(), 0);
                        }
                    } else {
                        // disconnect
                        mMaxwellBLE.disconnect();
                    }
                }
            };

    private CompoundButton.OnCheckedChangeListener mPNSwitchListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {

                    mSharedPref.enableDeviceIncomingCall(isChecked);
                }
            };

    private CompoundButton.OnCheckedChangeListener mOutOfRangeSwitchListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {

                    mSharedPref.enableDeviceOutOfRange(isChecked);
                    setLinkLossIndicator(isChecked);
                }
            };

    private CompoundButton.OnCheckedChangeListener mMoveAlertSwitchListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {

                    mSharedPref.enableDeviceMoveAlert(isChecked);
                    configAlertSetting();
                }
            };

    private CompoundButton.OnCheckedChangeListener mSOSSwitchListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {

                    mSharedPref.enableDeviceSOS(isChecked);
                    mMaxwellBLE.enableSOS(isChecked);

                    if (isChecked && mSharedPref.getEmergencyContactPhone().equals("")) {
                        DFEmergencyContact dlg = new DFEmergencyContact();
                        dlg.showHelper(mActivity);
                    }
                }
            };

    private CompoundButton.OnCheckedChangeListener mVibrateSwitchListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    mSharedPref.enableDeviceVibration(isChecked);
                    configSystemSetting();
                    }
                };

    @Override
    public void onDeviceConnect(MGPeripheral sender) {
//        mMaxwellBLE.sync();
//        mTextConnectionStat.setText("Connected");

    }

    @Override
    public void onDeviceDisconnect(MGPeripheral sender) {
    }

    @Override
    public void onConnectTimeOut(MGPeripheral sender) {

    }

    @Override
    public void onDeviceReady(MGPeripheral sender) {

    }

    @Override
    public void onSyncProgressUpdate(int progress) {
//        mSyncProgress.setProgress(progress);
    }

    @Override
    public void onSyncFinish() {
//        mSyncProgress.setVisibility(View.GONE);
    }

    @Override
    public void onSyncFail() {

    }
}
