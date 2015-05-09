package com.maxwell.bodysensor.dialogfragment.group;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.maxwell.bodysensor.MXWActivity;
import com.maxwell.bodysensor.MXWApp;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.group.GroupData;
import com.maxwell.bodysensor.data.group.GroupMemberData;
import com.maxwell.bodysensor.dialogfragment.DFBase;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgMessageYN;
import com.maxwell.bodysensor.util.UtilDBG;

import java.util.List;

/**
 * Created by ryanhsueh on 15/3/16.
 */
public class DFGroupInfo extends DFBase implements View.OnClickListener,
        DFAddGroupMember.OnAddNewMemberListener {

    private MXWActivity mActivity;
    private DBProgramData mPD;

    private ListView mLvMembers;
    private MemberListAdapter mAdapter;

    private GroupData mGroup;
    private List<GroupMemberData> mMemberList;

    @Override
    public String getDialogTag() {
        return MXWActivity.DF_GROUP_INFO;
    }

    @Override
    public int getDialogTheme() {
        return R.style.app_df_trans_rr;
    }

    @Override
    public void saveData() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UtilDBG.logMethod();

        mActivity = (MXWActivity) getActivity();
        mPD = DBProgramData.getInstance();

        View view = inflater.inflate(R.layout.df_group_info, container);

        view.findViewById(R.id.view_add_member).setOnClickListener(this);

        if (mGroup != null) {
            mMemberList = mPD.getListGroupMember(mGroup.group_Id);

            mLvMembers = (ListView) view.findViewById(R.id.list_group_member);
            mAdapter = new MemberListAdapter();
            mLvMembers.setAdapter(mAdapter);
            mLvMembers.setOnItemClickListener(mItemClickListener);

            view.findViewById(R.id.view_edit_group).setOnClickListener(this);

            setupTitleText(view, mGroup.name);
        }

        setupButtons(view);

        return view;
    }

    public void setGroupData(GroupData group) {
        mGroup = group;
    }

    @Override
    public void onClick(View v) {
        if (MXWApp.isClickFast(v)) {
            return;
        }

        DFBase dlg = null;
        switch(v.getId()) {
            case R.id.view_add_member:
                dlg = new DFAddGroupMember();
                ((DFAddGroupMember)dlg).setAddNewMemberListener(this);
                ((DFAddGroupMember)dlg).setGroupId(mGroup.group_Id);

                break;
            case R.id.view_edit_group:
                dlg = new DFSetupGroup();
                ((DFSetupGroup)dlg).setGroupData(mGroup);
                break;
        }

        if (dlg != null) {
            dlg.showHelper(mActivity);
        }
    }



    @Override
    public void onNewMemberUpdated() {
        mMemberList = mPD.getListGroupMember(mGroup.group_Id);
        mAdapter.notifyDataSetChanged();
    }

    public class MemberListAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;

        public MemberListAdapter() {
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rootView = convertView;

            MemberInfoHolder viewholder = null;
            if (rootView == null) {
                rootView = mLayoutInflater.inflate(R.layout.listitem_group_member_info, null);
                viewholder = new MemberInfoHolder(rootView);
                rootView.setTag(viewholder);
            } else {
                viewholder = (MemberInfoHolder) rootView.getTag();
            }

            GroupMemberData member = mMemberList.get(position);

            viewholder.textMemberId.setText(member.school_Id);
            viewholder.textMemberName.setText(member.name);

            if (member.photo != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(member.photo, 0, member.photo.length);
                viewholder.imgPhoto.setImageBitmap(bmp);
            }

            viewholder.viewDelete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    UtilDBG.d("[RYAN] Delete >>>>>>> position : " + position);
                    showDlgDelete(position);
                }
            });

            return rootView;
        }

        private void showDlgDelete(final int position) {
            DlgMessageYN dlg = new DlgMessageYN();
            dlg.setTitle(getString(R.string.dlg_delete_member_title));
            dlg.setDes(getString(R.string.dlg_delete_member_des));
            dlg.setPositiveButton(null, new DlgMessageYN.btnHandler() {
                @Override
                public boolean onBtnHandler() {
//                    GroupMemberData member = mMemberList.get(position);
//                    boolean success = mPD.deleteGroupMember(member.member_Id, member.targetDeviceMac);
//                    if (success) {
//                        mMemberList = mPD.getListGroupMember(mGroup.group_Id);
//                        mAdapter.notifyDataSetChanged();
//                    }
                    return true;
                }
            }).showHelper(mActivity);
        }
    }

    private class MemberInfoHolder {
        public TextView textMemberName;
        public TextView textMemberId;
        public ImageView imgPhoto;
        public View viewDelete;

        public MemberInfoHolder(View view) {
            textMemberName = (TextView) view.findViewById(R.id.text_member_name);
            textMemberId = (TextView) view.findViewById(R.id.text_member_id);
            imgPhoto = (ImageView) view.findViewById(R.id.img_member_photo);
            viewDelete = view.findViewById(R.id.bottom_delete);
        }
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (MXWApp.isClickFast(view)) {
                return;
            }

        }
    };

}
