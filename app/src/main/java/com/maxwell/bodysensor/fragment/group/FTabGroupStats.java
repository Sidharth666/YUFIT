package com.maxwell.bodysensor.fragment.group;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.maxwell.bodysensor.GroupActivity;
import com.maxwell.bodysensor.MXWApp;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.SharedPrefWrapper;
import com.maxwell.bodysensor.data.SleepLogData;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DSleepData;
import com.maxwell.bodysensor.data.DailyRecordData;
import com.maxwell.bodysensor.data.HourlyRecordData;
import com.maxwell.bodysensor.data.SleepScore;
import com.maxwell.bodysensor.data.group.GroupData;
import com.maxwell.bodysensor.data.group.GroupMemberData;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgSingleChoose;
import com.maxwell.bodysensor.listener.OnSyncDeviceListener;
import com.maxwell.bodysensor.ui.ViewChart;
import com.maxwell.bodysensor.util.UtilCVT;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilConst;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilLocale;
import com.maxwell.bodysensor.util.UtilTZ;
import com.maxwellguider.bluetooth.MGPeripheral;
import com.maxwellguider.bluetooth.activitytracker.GoalType;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;

import java.util.Calendar;
import java.util.List;

/**
 * Created by ryanhsueh on 15/4/28.
 */
public class FTabGroupStats extends Fragment implements View.OnClickListener,
        OnSyncDeviceListener {

    private static final int STATE_STEP = 0;
    private static final int STATE_ENERGY = 1;
    private static final int STATE_CALORIES = 2;
    private static final int STATE_DISTANCE = 3;
    private static final int STATE_SLEEP_EFFICIENCY = 4;
    private static final int STATE_SLEEP_HOURS = 5;

    private GroupActivity mActivity;
    private MGActivityTrackerApi mMaxwellBLE;
    private DBProgramData mPD;
    private SharedPrefWrapper mSharedPref;

    private ImageButton mBtnLogo;
    private ImageButton mBtnDeviceSync;
    private TextView mTextTitle;

    private ListView mLvMembers;
    private GroupListAdapter mAdapter;

    private RadioGroup mRGDWMY;
    private TextView mTextDuration;
    private View mViewPrevious;
    private View mViewNext;

    private View mViewSelectType;
    private TextView mTextStateType;

    private int mCurrentMode = 0;
    private UtilCalendar mTodayUTCDate;
    private UtilCalendar mTodayBegin;
    private UtilCalendar mTodayEnd;
    private UtilCalendar mLocalTime;
    private UtilCalendar mCalBegin;
    private UtilCalendar mCalEnd;
    private UtilCalendar mCalUTCDate;

    private int mTotalSleepLog;
    private int mIndexSleepLog;

    private List<GroupMemberData> mMemberList;

    //    private DlgIProgress mDlgIProgress = null;
    private ProgressDialog mProgressDialog = null;

    private int mSelectedType = STATE_STEP;
    private int mIndexSyncing = 0;

    private String[] mStateTypes;
    private int mAdd = 0;

    private GroupData mGroup;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UtilDBG.logMethod();

        return inflater.inflate(R.layout.fragment_group_stats, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = (GroupActivity) getActivity();
        mMaxwellBLE = MGActivityTracker.getInstance(mActivity);
        mPD = DBProgramData.getInstance();
        mSharedPref = SharedPrefWrapper.getInstance();

        mActivity.setOnSyncDeviceListener(this);

        mCurrentMode = 0;
        UtilCalendar calNow = new UtilCalendar(UtilTZ.getDefaultTZ());
        mTodayUTCDate = calNow;
        mTodayBegin = mTodayUTCDate.getFirstSecondBDay();
        mTodayEnd = mTodayUTCDate.getLastSecondBDay();

        View rootView = getView();

        // check group id
        long group_id = mSharedPref.getShowedGroupId();
        if (group_id == UtilConst.INVALID_INT) {
            List<GroupData> list = mPD.getListAllGroup();
            if (list.size() > 0) {
                group_id = mPD.getListAllGroup().get(0).group_Id;
                mSharedPref.setShowedGroupId(group_id);
            }
        }

        // initial group data & list view
        if (group_id != UtilConst.INVALID_INT) {
            initGroupStats(rootView, group_id);
        }

        mBtnLogo = (ImageButton) rootView.findViewById(R.id.button_home_logo);
        mBtnDeviceSync = (ImageButton) rootView.findViewById(R.id.button_home_sync);

        mRGDWMY = (RadioGroup) rootView.findViewById(R.id.rg_trend_dwmy);
        mRGDWMY.check(R.id.rgb_trend_day);
        mRGDWMY.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup rgMode, int iID) {
                updateMode();
            }
        });

        mViewPrevious = rootView.findViewById(R.id.btn_trend_previous);
        mTextDuration = (TextView) rootView.findViewById(R.id.text_trend_range);
        mViewNext = rootView.findViewById(R.id.btn_trend_next);
        mViewSelectType = rootView.findViewById(R.id.view_select_state_type);

        mBtnLogo.setOnClickListener(this);
        mBtnDeviceSync.setOnClickListener(this);

        mViewPrevious.setOnClickListener(this);
        mViewNext.setOnClickListener(this);
        mViewSelectType.setOnClickListener(this);

        mTextStateType = (TextView) rootView.findViewById(R.id.text_state_type);

        // initial State Type array
        mStateTypes = new String[6];
        mStateTypes[STATE_STEP] = getString(R.string.strSteps);
        mStateTypes[STATE_ENERGY] = getString(R.string.faEnergy);
        mStateTypes[STATE_CALORIES] = getString(R.string.strCalories);
        mStateTypes[STATE_DISTANCE] = getString(R.string.strDistance);
        mStateTypes[STATE_SLEEP_EFFICIENCY] = getString(R.string.fsSleepEfficiency);
        mStateTypes[STATE_SLEEP_HOURS] = getString(R.string.fsDeepSleep);
    }

    @Override
    public void onResume() {
        super.onResume();

        int i = mSharedPref.getAppTabTrendsMode();
        if (i != -1) {
            switch(i) {
                case ViewChart.AD:
                case ViewChart.SD:
                    mRGDWMY.check(R.id.rgb_trend_day);
                    break;
                case ViewChart.AW:
                case ViewChart.SW:
                    mRGDWMY.check(R.id.rgb_trend_week);
                    break;
                case ViewChart.AM:
                case ViewChart.SM:
                    mRGDWMY.check(R.id.rgb_trend_month);
                    break;
                case ViewChart.AY:
                case ViewChart.SY:
                    mRGDWMY.check(R.id.rgb_trend_year);
                    break;
                default:
                    UtilDBG.e("getAppTabTrendsMode() not expected mode");
            }
            mSharedPref.setAppTabTrendsMode(-1);
        }

        updateMode();
    }


    private void initGroupStats(View rootView, long group_id) {
        mGroup = mPD.getGroupData(group_id);
        if (mGroup != null) {
            mMemberList = mPD.getListGroupMember(group_id);

            initMemberSyncState();

            mLvMembers = (ListView) rootView.findViewById(R.id.list_group_member_stats);
            mAdapter = new GroupListAdapter();
            mLvMembers.setAdapter(mAdapter);

            mTextTitle = (TextView) rootView.findViewById(R.id.title_group_name);
            mTextTitle.setText(mGroup.name);
        }
    }

    private void initMemberSyncState() {

        // Member sync state is true if he was synced within 1 hour
        UtilCalendar calNow = new UtilCalendar(UtilTZ.getDefaultTZ());
        calNow.add(Calendar.MINUTE, -60);

        UtilDBG.i("[RYAN] FTabGroupStats > initGroupStats > calNow before 1 hour : " + UtilLocale.calToString(calNow, UtilLocale.DateFmt.YMDHMa));

        // Mark for test
//        for (GroupMemberData member : mMemberList) {
//            DeviceData device = mPD.getGroupDeviceData(member.ADTAddress);
//            if (device != null) {
//                long lastSyncTime = device.getLastSyncTime();
//                if (lastSyncTime > calNow.getUnixTime()) {
//                    member.isSynced = true;
//                }
//            }
//        }
    }

    private void updateMode() {
        boolean bActivity = true;
        int iNewMode = ViewChart.AD;

        if (mSelectedType==STATE_SLEEP_EFFICIENCY || mSelectedType==STATE_SLEEP_HOURS) {
            bActivity = false;
        }

        switch(mRGDWMY.getCheckedRadioButtonId()) {
            case R.id.rgb_trend_day:
                iNewMode = (bActivity ? ViewChart.AD : ViewChart.SD);
                break;
            case R.id.rgb_trend_week:
                iNewMode = (bActivity ? ViewChart.AW : ViewChart.SW);
                break;
            case R.id.rgb_trend_month:
                iNewMode = (bActivity ? ViewChart.AM : ViewChart.SM);
                break;
            case R.id.rgb_trend_year:
                iNewMode = (bActivity ? ViewChart.AY : ViewChart.SY);
                break;
            default:
                UtilDBG.e("FTrend updateMode, rg dwmy unexpected");
                break;
        }

//        if (mCurrentMode != iNewMode) {
        mCurrentMode = iNewMode;

        calCurrentTime();
        updateView();
//        }
    }

    private void calCurrentTime() {
        mLocalTime = new UtilCalendar(UtilTZ.getDefaultTZ());
        mCalBegin = mLocalTime.getFirstSecondBDay();
        mCalEnd = mLocalTime.getLastSecondBDay();
    }

    /*
     iAdd = 1, index = the first one
     iAdd = -1. index = the last one
      */
    private void initDateIndex(int iAdd, String address) {
        UtilCalendar calUTCDate = mLocalTime.getFirstSecondBDay();
        mTotalSleepLog = mPD.queryGroupSleepLogSize(calUTCDate, address);
        if (mTotalSleepLog<=0) {
            mIndexSleepLog = -1;
        } else {
            if (iAdd>0) {
                mIndexSleepLog = 0;
            } else if (iAdd<0) {
                mIndexSleepLog = mTotalSleepLog-1;
            } else {
                UtilDBG.e("not expected");
                mIndexSleepLog = 0;
            }
        }
    }

    // update view, base on mode, time
    private void updateView() {
        switch(mCurrentMode) {
            case ViewChart.AD:
            case ViewChart.SD:
                mCalUTCDate = mLocalTime.getFirstSecondBDay();
                mCalBegin = mCalUTCDate.getFirstSecondBDay();
                mCalEnd = mCalUTCDate.getLastSecondBDay();
                mTextDuration.setText(UtilLocale.calToString(mLocalTime, UtilLocale.DateFmt.YMDWeek));
                break;
            case ViewChart.AW:
            case ViewChart.SW:
                mCalUTCDate = mLocalTime.getFirstSecondBDay();
                mCalBegin = mCalUTCDate.getFirstSecondBWeek();
                mCalEnd = mCalUTCDate.getLastSecondBWeek();
                mTextDuration.setText(UtilLocale.calToString(mCalBegin, UtilLocale.DateFmt.YMD) + " - " + UtilLocale.calToString(mCalEnd, UtilLocale.DateFmt.YMD));
                break;
            case ViewChart.AM:
            case ViewChart.SM:
                mCalUTCDate = mLocalTime.getFirstSecondBDay();
                mCalBegin = mCalUTCDate.getFirstSecondBMonth();
                mCalEnd = mCalUTCDate.getLastSecondBMonth();
                mTextDuration.setText(UtilLocale.calToString(mCalUTCDate, UtilLocale.DateFmt.YM));
                break;
            case ViewChart.AY:
            case ViewChart.SY:
                mCalUTCDate = mLocalTime.getFirstSecondBDay();
                mCalBegin = mCalUTCDate.getFirstSecondBYear();
                mCalEnd = mCalUTCDate.getLastSecondBYear();
                mTextDuration.setText(Integer.toString(mCalUTCDate.get(Calendar.YEAR)));
                break;
            default:
                UtilDBG.e("FTrend updateView, current mode unexpected");
                break;
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void adjustDate(int iAdd) {
        mAdd = iAdd;
        switch(mCurrentMode) {
            case ViewChart.AD:
            case ViewChart.SD:
                mLocalTime.add(Calendar.DAY_OF_MONTH, iAdd);
                break;
            case ViewChart.AW:
            case ViewChart.SW:
                mLocalTime.add(Calendar.WEEK_OF_YEAR, iAdd);
                break;
            case ViewChart.AM:
            case ViewChart.SM:
                mLocalTime.add(Calendar.MONTH, iAdd);
                break;
            case ViewChart.AY:
            case ViewChart.SY:
                mLocalTime.add(Calendar.YEAR, iAdd);
                break;
            default:
                UtilDBG.e("FTrend adjustDate, current mode unexpected");
                break;
        }

        updateView();
    }

    private void startSyncDevice() {
        boolean syning = false;
        int size = mMemberList.size();
        if (size > 0) {
            GroupMemberData member;

            // get the first member to sync
            for (int i=mIndexSyncing ; i<size ; i++) {
                member = mMemberList.get(i);
                if (!member.isSynced && !member.targetDeviceMac.equals("")) {
                    // Found, connect it !!
                    mMaxwellBLE.connect(member.targetDeviceMac, 10000);
                    mActivity.updateGroupMemberForSync(member);

                    mIndexSyncing = i;
                    syning = true;
                    showProgress();

                    break;
                }
            }

            if (!syning) {
                hideProgress();
            } else {
                updateSyncProgress();
            }
        }
    }

    private void syncNext(boolean success) {
        mMaxwellBLE.disconnect();

        if (mMemberList == null) {
            hideProgress();
            return;
        }

        if (mIndexSyncing >= mMemberList.size()) {
            hideProgress();
            return;
        }

        mMemberList.get(mIndexSyncing).isSynced = success;

        if (success) {
            mAdapter.notifyDataSetChanged();
        }

        mIndexSyncing++;
        if (mIndexSyncing < mMemberList.size()) {
            Handler handler = new Handler();
            handler.postDelayed(mRealSyncADT, 1000);
        } else {
            hideProgress();
        }
    }


    private void showProgress() {
//        if (mDlgIProgress == null) {
//            mDlgIProgress = new DlgIProgress();
//            mDlgIProgress.setDescription(getString(R.string.ipSyncing));
//            mDlgIProgress.showHelper(getActivity());
//        }

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setMessage(getString(R.string.ipSyncing));
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
    }
    private void hideProgress() {
//        if (mDlgIProgress!=null) {
//            mDlgIProgress.dismissAllowingStateLoss();
//            mDlgIProgress = null;
//        }

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
    private void updateSyncProgress() {
        if (mProgressDialog != null) {
//            int size = mMemberList.size();
//            int index = mIndexSyncing + 1;
//            if (index > size) {
//                index = size;
//            }
            int progress = (int)((float)mIndexSyncing/mMemberList.size() * 100);
            mProgressDialog.setProgress(progress);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mActivity.setOnSyncDeviceListener(null);
    }

    @Override
    public void onClick(View v) {
        if (v!=mViewPrevious && v!=mViewNext && MXWApp.isClickFast(v)) {
            return;
        }

        if (v == null) {
            return;
        } else if (v == mBtnLogo) {
            mActivity.showSwitchModeDialog();
        } else if (v == mBtnDeviceSync) {
            mIndexSyncing = 0;
            startSyncDevice();
        } else if (v==mViewPrevious || v==mViewNext){
            int iAdd = 0;
            switch (v.getId()) {
                case R.id.btn_trend_previous:
                    iAdd = -1;
                    break;
                case R.id.btn_trend_next:
                    iAdd = 1;
                    break;
                default:
                    UtilDBG.e("unexpected in clickImgBtn OnClickListener    1 ");
                    break;
            }

            adjustDate(iAdd);
        } else if (v == mViewSelectType) {
            final DlgSingleChoose dlg = new DlgSingleChoose();
            dlg.setListData(mStateTypes);
            dlg.setSelected(mSelectedType);
            dlg.setPositiveButton(null, new DlgSingleChoose.btnHandler() {
                @Override
                public boolean onBtnHandler() {
                    mSelectedType = dlg.getSelected();
                    mTextStateType.setText(mStateTypes[mSelectedType]);
                    updateMode();
                    return true;
                }
            }).showHelper(mActivity);
        }

    }

    private Runnable mRealSyncADT =  new Runnable() {
        @Override
        public void run() {
            startSyncDevice();
        }
    };

    @Override
    public void onDeviceConnect(MGPeripheral sender) {
    }

    @Override
    public void onDeviceDisconnect(MGPeripheral sender) {
    }

    @Override
    public void onConnectTimeOut(MGPeripheral sender) {
        syncNext(false);
    }

    @Override
    public void onDeviceReady(MGPeripheral sender) {
        mMaxwellBLE.sync();
    }

    @Override
    public void onSyncProgressUpdate(int progress) {
        // do nothing
    }

    @Override
    public void onSyncFinish() {
        syncNext(true);
    }

    @Override
    public void onSyncFail() {
        syncNext(false);
    }

    private class GroupListAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;

        public GroupListAdapter() {
            mLayoutInflater = LayoutInflater.from(mActivity);
        }

        @Override
        public int getCount() {
            return mMemberList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return mMemberList.get(position).member_Id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView = convertView;

            MemberStatsHolder viewholder = null;
            if (rootView == null) {
                rootView = mLayoutInflater.inflate(R.layout.listitem_group_member_stats, null);
                viewholder = new MemberStatsHolder(rootView);
                rootView.setTag(viewholder);
            } else {
                viewholder = (MemberStatsHolder) rootView.getTag();
            }

            GroupMemberData member = mMemberList.get(position);

            viewholder.textMemberId.setText(member.school_Id);
            viewholder.textMemberName.setText(member.name);

            // update member photo
            if (member.photo != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(member.photo, 0, member.photo.length);
                viewholder.imgPhoto.setImageBitmap(bmp);
            }

            // update member sync state
            boolean hasDevice = false;
            if (!member.targetDeviceMac.equals("")) {
                hasDevice = true;
                if (member.isSynced) {
                } else {
                }
            }

            if (!hasDevice) {
                viewholder.textState.setText(R.string.str_empty);
            } else {
                UtilDBG.d("[RYAN] getView > mCurrentMode : " + mCurrentMode);
                double dValue = 0;
                switch(mCurrentMode) {
                    case ViewChart.AD:
                        dValue = getDailyActivity(member.targetDeviceMac);
                        break;
                    case ViewChart.SD:
                        dValue = getDailySleep(member.targetDeviceMac);
                        break;
                    case ViewChart.SW:
                    case ViewChart.SM:
                    case ViewChart.SY:
                        dValue = getWMYSleep(member.targetDeviceMac);
                        break;
                    default:
                        dValue = getWMYActivity(member.targetDeviceMac, mGroup.daily_goal);
                }

//                Logger.d("[RYAN] getView > position : " + position + ", dValue : " + dValue + ", mSelectedType : " + mSelectedType);
                viewholder.textState.setText(getStrStateValue(dValue));
            }

            if (isDailyGoalAchieved(member.targetDeviceMac)) {
            } else {
                viewholder.imgGoal.setImageResource(R.drawable.goal_not_achieve);
            }


            return rootView;
        }

        private boolean isDailyGoalAchieved(String address) {
            List<HourlyRecordData> HourlyData = mPD.queryGroupHourlyData(
                    mTodayBegin, mTodayEnd, address);

            double dStep = 0.0f;
            double dEnergy = 0.0f;
            for (HourlyRecordData data : HourlyData) {
                dStep += data.mStep;
                dEnergy += data.mAppEnergy;
            }

            if (mGroup.goal_type == GoalType.ENERGY) {
                return (dEnergy >= mGroup.daily_goal);
            } else {
                return (dStep >= mGroup.daily_goal);
            }

        }

        private double getDailyActivity(String address) {
            double dValue = 0.0f;

            // get hourly record
            List<HourlyRecordData> HourlyData = mPD.queryGroupHourlyData(
                    mCalBegin, mCalEnd, address);
            for (HourlyRecordData data : HourlyData) {
                switch (mSelectedType) {
                    case STATE_STEP:
                        dValue += data.mStep;
                        break;
                    case STATE_ENERGY:
                        dValue += data.mAppEnergy;
                        break;
                    case STATE_CALORIES:
                        dValue += data.mCalories;
                        break;
                    case STATE_DISTANCE:
                        dValue += data.mDistance;
                        break;
                    default:
                        dValue = 0;
                }

            }

            return dValue;
        }

        private double getDailySleep(String address) {
            if (mTotalSleepLog==0) {
                initDateIndex(mAdd, address);
            } else {
                mIndexSleepLog += mAdd;

                UtilCalendar calUTCDate = mLocalTime.getFirstSecondBDay();
                mTotalSleepLog = mPD.queryGroupSleepLogSize(calUTCDate, address);
                if (mIndexSleepLog<0) {
                    // yesterday
                    if (mTotalSleepLog<=0) {
                        mIndexSleepLog = -1;
                    } else {
                        mIndexSleepLog = mTotalSleepLog-1;
                    }
                } else if (mIndexSleepLog >= mTotalSleepLog) {
                    // tomorrow
                    if (mTotalSleepLog<=0) {
                        mIndexSleepLog = -1;
                    } else {
                        mIndexSleepLog = 0;
                    }
                }
            }

            SleepLogData log = null;
            SleepScore score = null;
            if (mIndexSleepLog>=0) {
                log = mPD.queryGroupSleepLog(mCalUTCDate, mIndexSleepLog, address);
            }

            if (log!=null) {
                score = mPD.calculateGroupSleepScore(log.mDate, log.mStartTime, log.mStopTime, address);
            }

            UtilDBG.d("[RYAN] getView > score : " + score);

            double dValue;
            double dDuration = 0.0f;
            double dEfficiency = 0.0f;
            if (score!=null) {
                dEfficiency = score.mSleepScore;
                dDuration = score.mDiffMinute;
            }

            if (mSelectedType == STATE_SLEEP_EFFICIENCY) {
                dValue = dEfficiency;
            } else {
                dValue = dDuration;
            }

            return dValue;
        }

        private double getWMYActivity(String address, int daily_goal) {
            double dValue = 0.0f;

            // get daily record record
            List<DailyRecordData> DailyData = mPD.queryGroupDailyData(
                    mCalBegin, mCalEnd, mTodayUTCDate, address, daily_goal);
            for (DailyRecordData data: DailyData) {
                switch (mSelectedType) {
                    case STATE_STEP :
                        dValue += data.mStep;
                        break;
                    case STATE_ENERGY :
                        dValue += data.mAppEnergy;
                        break;
                    case STATE_CALORIES :
                        dValue += data.mCalories;
                        break;
                    case STATE_DISTANCE :
                        dValue += data.mDistance;
                        break;
                    default :
                        dValue = 0;
                }
            }

            return dValue;
        }

        private double getWMYSleep(String address) {
            double dValue;
            double dDuration = 0.0f;
            double dEfficiency = 0.0f;

            List<DSleepData> dSleepDatas = mPD.queryGroupDSleepData(mCalBegin, mCalEnd, address);
            for (DSleepData data : dSleepDatas) {
                dEfficiency += data.mScore;
                dDuration += data.mDuration;
            }

            int size = dSleepDatas.size();
            if (size > 0) {
                dEfficiency /= size;
                dDuration /= size;
            }

            if (mSelectedType == STATE_SLEEP_EFFICIENCY) {
                dValue = dEfficiency;
            } else {
                dValue = dDuration;
            }

            return dValue;
        }

        private String getStrStateValue(double dValue) {
            StringBuilder sb = new StringBuilder();
            switch (mSelectedType) {
                case STATE_STEP :
                case STATE_ENERGY :
                    if (dValue > 10000) {
                        dValue = UtilCVT.doubleToInt(dValue / 1000.0d);
                        sb.append(String.valueOf((int) dValue)).append(" k");
                    } else {
                        sb.append(String.valueOf((int) dValue));
                    }
                    break;
                case STATE_CALORIES :
                    sb.append(UtilCVT.doubleToString(dValue, 2));
                    break;
                case STATE_DISTANCE :
                    sb.append(UtilCVT.doubleToString(dValue, 2))
                            .append(" ")
                            .append(getString(R.string.kilometers_short));
                    break;
                case STATE_SLEEP_EFFICIENCY :
                    sb =
                            sb.append(Integer.toString(UtilCVT.doubleToInt(dValue)))
                                    .append(" ")
                                    .append(getString(R.string.percentage));
                    break;
                case STATE_SLEEP_HOURS :
                    int iDuration = UtilCVT.doubleToInt(dValue);
                    int h = iDuration / 60;
                    int m = iDuration % 60;
                    sb = new StringBuilder();
                    sb.append(h).append(getString(R.string.trend_sleep_h))
                            .append(m).append(getString(R.string.trend_sleep_m));
                    break;
                default :
                    sb.append(getString(R.string.str_empty));
            }

            return sb.toString();
        }
    }

    private class MemberStatsHolder {
        public TextView textMemberName;
        public TextView textMemberId;
        public TextView textState;
        public ImageView imgSync;
        public ImageView imgPhoto;
        public ImageView imgGoal;

        public MemberStatsHolder(View view) {
            textMemberName = (TextView) view.findViewById(R.id.text_member_name);
            textMemberId = (TextView) view.findViewById(R.id.text_member_id);
            textState = (TextView) view.findViewById(R.id.text_member_state);
            imgSync = (ImageView) view.findViewById(R.id.img_sync_state);
            imgPhoto = (ImageView) view.findViewById(R.id.img_member_photo);
            imgGoal = (ImageView) view.findViewById(R.id.img_member_goal);
        }
    }
}
