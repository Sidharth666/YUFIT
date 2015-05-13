package com.maxwell.bodysensor.dialogfragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maxwell.bodysensor.MXWActivity;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DeviceData;
import com.maxwell.bodysensor.data.ProfileData;
import com.maxwell.bodysensor.data.group.GroupMemberData;
import com.maxwell.bodysensor.dialogfragment.group.DFAddGroupMember;
import com.maxwell.bodysensor.dialogfragment.group.DFAddNewGroup;
import com.maxwell.bodysensor.fragment.FAdd60;
import com.maxwell.bodysensor.fragment.FAddSearch;
import com.maxwell.bodysensor.fragment.FAddSelectType;
import com.maxwell.bodysensor.fragment.FAddTrouble;
import com.maxwell.bodysensor.listener.OnPairDeviceListener;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilTZ;
import com.maxwellguider.bluetooth.AdvertisingData;
import com.maxwellguider.bluetooth.MGPeripheral;
import com.maxwellguider.bluetooth.MGPeripheral.DeviceType;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;

public class DFAddNewDevice extends DFBase implements OnPairDeviceListener {

    private MXWActivity mActivity;
    private DBProgramData mPD;
    private MGActivityTrackerApi mMaxwellBLE;

    private Fragment mFragment;
    private static final int NUM_PAGES = 5;

    private static final int INDEX_SELECT_DEVICE = 0;
    private static final int INDEX_SEARCH = 1;
    private static final int INDEX_TROUBLE = 2;
    private static final int INDEX_ADD_60 = 3;
    private static final int INDEX_EDIT_DEVICE = 4;

    private FAddSelectType mFAddSelectType = null;
    private FAddSearch mFAddSearch = null;
    private FAddTrouble mFAddTrouble = null;
    private FAdd60 mFAdd60 = null;
    private DFDeviceInfo mDFDeviceInfo = null;

    private AdvertisingData mTargetDevice;

    private ViewPager mViewPager = null;

    @Override
    public String getDialogTag() {
        return MainActivity.DF_ADD_NEW_DEVICE;
    }

    @Override
    public int getDialogTheme() {
        return R.style.app_df_trans_rr;
    }

    @Override
    public void saveData() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        UtilDBG.logMethod();

        mActivity = (MXWActivity) getActivity();
        mMaxwellBLE = MGActivityTracker.getInstance(mActivity);

        View view = inflater.inflate(R.layout.df_add_new_device, (mFragment==null) ? container : null);

        mFAddSelectType = new FAddSelectType();
        mFAddSearch = new FAddSearch();
        mFAddTrouble = new FAddTrouble();
        mFAdd60 = new FAdd60();
        mDFDeviceInfo = new DFDeviceInfo();

        mFAddSelectType.setDFAddNew(this);
        mFAddSelectType.setIsFirstLaunch(mFragment != null);
        mFAddSearch.setDFAddNew(this);
        mFAddTrouble.setDFAddNew(this);
        mFAdd60.setDFAddNew(this);
        mDFDeviceInfo.setDFAddNew(this);

        mPD = DBProgramData.getInstance();

        setCancelable(false);

        initViewPager(view);

        return view;
    }

    private void initViewPager(View view) {
        MyPagerAdapter pageAdapter = new MyPagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) view.findViewById(R.id.addnew_view_pager);
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageSelected(int position) {
                switch(position) {
                    case INDEX_SELECT_DEVICE:
                        mActivity.stopScanDevice();
                        break;
                    case INDEX_SEARCH:
                        mActivity.startScanDevice(DFAddNewDevice.this);

                        break;
                    case INDEX_EDIT_DEVICE:
                        DeviceData device = new DeviceData();
                        if (inUserMode()) {
                            // User Mode
                            device.profileId = mPD.getProfileId();
                        } else {
                            // Group Mode
                            GroupMemberData member = getGroupMember();
                            device.profileId = member.member_Id;
                        }
                        device.displayName = mTargetDevice.deviceName;
                        device.mac = mTargetDevice.address;
                        device.lastDailySyncTime = 0;
                        device.lastHourlySyncTime = 0;
                        device.lastTimezoneDiff = UtilTZ.getDefaultRawOffset();

                        mDFDeviceInfo.setDeviceData(device);
                        mDFDeviceInfo.updateView();
                        break;
                    default:
                        break;
                }
            }
        } );
    }

    private boolean inUserMode() {
        return (mActivity instanceof MainActivity);
    }

    public void cancelPairDevice() {
            dismiss();
    }

    public void skipPairDevice() {
         if (mFragment instanceof DFAddNewGroup) {
            ((DFAddNewGroup) mFragment).skipPairDevice();
        } else if (mFragment instanceof DFAddGroupMember) {
            ((DFAddGroupMember) mFragment).skipPairDevice();
        }
    }

    public void page20Back() {
        mViewPager.setCurrentItem(INDEX_SELECT_DEVICE);
    }

    public void page30Next() {
        mViewPager.setCurrentItem(INDEX_SEARCH);
    }

    public void goToEditDevice() { mViewPager.setCurrentItem(INDEX_EDIT_DEVICE); }

    public void finishEditDevice() {
         if (mFragment instanceof DFAddNewGroup) {
            ((DFAddNewGroup) mFragment).addnewFinish();
        } else if (mFragment instanceof DFAddGroupMember) {
            ((DFAddGroupMember) mFragment).addnewFinish();
        } else {
            dismiss();
        }
    }

    public void showPairFailed() {
        mViewPager.setCurrentItem(INDEX_TROUBLE);
    }

    public void setFragment(Fragment f) {
        mFragment = f;
    }

    public void setTargetDevice(AdvertisingData device) {
        mTargetDevice = device;

        mMaxwellBLE.connect(device.address, 10000);
    }

    private GroupMemberData getGroupMember() {
        GroupMemberData member = null;
        if (mFragment instanceof DFAddNewGroup) {
            member = ((DFAddNewGroup) mFragment).getGroupMemberData();
        } else if (mFragment instanceof DFAddGroupMember) {
            member = ((DFAddGroupMember) mFragment).getGroupMemberData();
        }
        return member;
    }

    @Override
    public void onDeviceDiscover(MGPeripheral sender, AdvertisingData device) {
        mFAddSearch.addScanDevice(device);
    }

    @Override
    public void onDeviceConnect(MGPeripheral sender) {
        mActivity.stopScanDevice();
        mFAddSearch.pairFinished();
        Log.e("DFAdd", "inside save");
        if (inUserMode()) {
            Log.e("DFAdd", "inside user mode");
            ProfileData profile = new ProfileData();
            profile.name = "Mukund";

            mPD.setTargetDeviceMac(sender.getTargetAddress());
            mPD.saveUserProfile(profile);
        } else {
            GroupMemberData member = getGroupMember();
            mPD.setMemberTargetDeviceMac(member.member_Id, sender.getTargetAddress());
        }

        goToEditDevice();
    }

    @Override
    public void onConnectTimeOut(MGPeripheral sender) {
        mFAddSearch.pairFinished();
        showPairFailed();
    }

    @Override
    public void onDeviceReady(MGPeripheral sender) {
        DeviceType deviceType = sender.getDeviceType(sender.getTargetAddress());
        if (deviceType == DeviceType.ENERGY_CAPSULE) {
            mMaxwellBLE.disconnect();
        }
    }

    class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
            case INDEX_SELECT_DEVICE:
                return mFAddSelectType;
            case INDEX_SEARCH:
                return mFAddSearch;
            case INDEX_TROUBLE:
                return mFAddTrouble;
            case INDEX_ADD_60:
                return mFAdd60;
            case INDEX_EDIT_DEVICE:
                return mDFDeviceInfo;
            }

            UtilDBG.e("DFAddNewDevice, getItem, position" + Integer.toString(position));
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
