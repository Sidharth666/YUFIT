package com.maxwell.bodysensor.dialogfragment.group;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maxwell.bodysensor.GroupActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.SharedPrefWrapper;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.group.GroupData;
import com.maxwell.bodysensor.data.group.GroupMemberData;
import com.maxwell.bodysensor.dialogfragment.DFAddNewDevice;
import com.maxwell.bodysensor.dialogfragment.DFBase;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by ryanhsueh on 15/3/17.
 */
public class DFAddNewGroup extends DFBase {

    private static final int NUM_PAGES = 3;

    private static final int INDEX_SETUP_GROUP = 0;
    private static final int INDEX_MEMBER_PROFILE = 1;
    private static final int INDEX_PAIR_DEVICE = 2;

    private GroupActivity mActivity;
    private DBProgramData mPD;

    private DFSetupGroup mDFSetupGroup = null;
    private DFAddNewDevice mDFAddNewDevice = null;

    private ViewPager mViewPager = null;

    private GroupData mNewGroup;
    private GroupMemberData mNewGroupMember;

    private OnAddNewGroupListener mAddNewGroupListener;
    public interface OnAddNewGroupListener {
        public void onNewGroupUpdated();
    }

    public void setAddNewGroupListener(OnAddNewGroupListener listener) {
        mAddNewGroupListener = listener;
    }

    @Override
    public String getDialogTag() {
        return GroupActivity.DF_ADD_NEW_GROUP;
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

        mActivity = (GroupActivity) getActivity();
        mPD = DBProgramData.getInstance();

        View view = inflater.inflate(R.layout.df_add_new_group, container);

        mDFSetupGroup = new DFSetupGroup();
        mDFAddNewDevice = new DFAddNewDevice();

        mDFSetupGroup.setDFAddNewGroup(this);
        mDFAddNewDevice.setFragment(this);

        MyPagerAdapter pageAdapter = new MyPagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) view.findViewById(R.id.pager_new_group);
        mViewPager.setAdapter(pageAdapter);

        setCancelable(false);

        if (mNewGroup == null) {
            mNewGroup = new GroupData();
        }

        setupGroup();

        return view;
    }

    public void setupGroupFinsh(GroupData group) {
        // save group data
        mNewGroup = group;
        mNewGroup.group_Id = mPD.addNewGroup(mNewGroup);

        setupMemberProfile();
    }

    public void addNewMember(GroupMemberData member) {
        member.group_Id = mNewGroup.group_Id;
        mNewGroupMember = member;
        mNewGroupMember.member_Id = mPD.addNewMember(member);
    }

    public GroupMemberData getGroupMemberData() {
        return mNewGroupMember;
    }

    public void typeBack() {
        dismiss();
    }
    public void setupGroup() {
        mViewPager.setCurrentItem(INDEX_SETUP_GROUP);
    }

    public void setupMemberProfile() {
        mViewPager.setCurrentItem(INDEX_MEMBER_PROFILE);
    }

    public void pairNewDevice() {
        mViewPager.setCurrentItem(INDEX_PAIR_DEVICE);
    }

    public void skipPairDevice() {
        addnewFinish();
    }

    public void addnewFinish() {
        mActivity.showContainerGroup();

        if (mAddNewGroupListener != null) {
            mAddNewGroupListener.onNewGroupUpdated();
        }

        dismiss();
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case INDEX_SETUP_GROUP:
                    return mDFSetupGroup;
                case INDEX_MEMBER_PROFILE:
                case INDEX_PAIR_DEVICE:
                    return mDFAddNewDevice;
            }

            UtilDBG.e("DFAddNewGroup, getItem, position" + Integer.toString(position));
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
