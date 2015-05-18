package com.maxwell.bodysensor.dialogfragment.group;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maxwell.bodysensor.MXWActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.group.GroupMemberData;
import com.maxwell.bodysensor.dialogfragment.DFAddNewDevice;
import com.maxwell.bodysensor.dialogfragment.DFBase;
import com.maxwell.bodysensor.util.UtilConst;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by ryanhsueh on 15/3/23.
 */
public class DFAddGroupMember extends DFBase {

    private static final int NUM_PAGES = 2;

    private static final int INDEX_MEMBER_PROFILE = 0;
    private static final int INDEX_PAIR_DEVICE = 1;

    private MXWActivity mActivity;
    private DBProgramData mPD;

    private DFAddNewDevice mDFAddNewDevice = null;

    private ViewPager mViewPager = null;

    private long mGroupId = UtilConst.INVALID_INT;
    private GroupMemberData mNewGroupMember;

    private OnAddNewMemberListener mAddNewMemberListener;
    public interface OnAddNewMemberListener {
        public void onNewMemberUpdated();
    }

    public void setAddNewMemberListener(OnAddNewMemberListener listener) {
        mAddNewMemberListener = listener;
    }

    @Override
    public String getDialogTag() {
        return MXWActivity.DF_ADD_GROUP_MEMBER;
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

        if (mGroupId == UtilConst.INVALID_INT) {
            dismiss();
        }

        mActivity = (MXWActivity) getActivity();
        mPD = DBProgramData.getInstance();

        View view = inflater.inflate(R.layout.df_add_new_group, container);

        mDFAddNewDevice = new DFAddNewDevice();

        mDFAddNewDevice.setFragment(this);

        MyPagerAdapter pageAdapter = new MyPagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) view.findViewById(R.id.pager_new_group);
        mViewPager.setAdapter(pageAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageSelected(int position) {
                switch(position) {
                    case INDEX_MEMBER_PROFILE:
                        break;
                    default:
                        break;
                }
            }
        } );

        return view;
    }

    public void setGroupId(long group_id) {
        mGroupId = group_id;
    }

    public void addNewMember(GroupMemberData member) {
        member.group_Id = mGroupId;
        mNewGroupMember = member;
        mNewGroupMember.member_Id = mPD.addNewMember(member);
    }

    public GroupMemberData getGroupMemberData() {
        return mNewGroupMember;
    }

    public void pairNewDevice() {
        mViewPager.setCurrentItem(INDEX_PAIR_DEVICE);
    }

    public void skipPairDevice() {
        addnewFinish();
    }

    public void addnewFinish() {
        if (mAddNewMemberListener != null) {
            mAddNewMemberListener.onNewMemberUpdated();
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
                case INDEX_MEMBER_PROFILE:
                case INDEX_PAIR_DEVICE:
                    return mDFAddNewDevice;
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
