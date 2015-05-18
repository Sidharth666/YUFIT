package com.maxwell.bodysensor.fragment.group;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.maxwell.bodysensor.GroupActivity;
import com.maxwell.bodysensor.MXWApp;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.SharedPrefWrapper;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.group.GroupData;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgMessageYN;
import com.maxwell.bodysensor.dialogfragment.group.DFAddNewGroup;
import com.maxwell.bodysensor.dialogfragment.group.DFGroupInfo;
import com.maxwell.bodysensor.util.UtilDBG;

import java.util.List;

/**
 * Created by ryanhsueh on 15/4/28.
 */
public class FTabGroups extends Fragment implements View.OnClickListener,
        DFAddNewGroup.OnAddNewGroupListener {

    private GroupActivity mActivity;
    private DBProgramData mPD;
    private SharedPrefWrapper mSharedPref;

    private ImageButton mBtnLogo;
    private View mViewAddGroup;

    private ListView mLvGroups;
    private GroupListAdapter mAdapter;

    private List<GroupData> mGroupList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UtilDBG.logMethod();

        return inflater.inflate(R.layout.fragment_groups, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = (GroupActivity) getActivity();
        mPD = DBProgramData.getInstance();
        mSharedPref = SharedPrefWrapper.getInstance();

        View rootView = getView();

        mGroupList = mPD.getListAllGroup();

        mLvGroups = (ListView) rootView.findViewById(R.id.list_groups);
        mAdapter = new GroupListAdapter();
        mLvGroups.setAdapter(mAdapter);
        mLvGroups.setOnItemClickListener(mItemClickListener);

        mBtnLogo = (ImageButton) rootView.findViewById(R.id.button_home_logo);
        mViewAddGroup = rootView.findViewById(R.id.view_add_group);

        mBtnLogo.setOnClickListener(this);
        mViewAddGroup.setOnClickListener(this);
    }

    @Override
    public void onNewGroupUpdated() {
        mGroupList = mPD.getListAllGroup();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (MXWApp.isClickFast(v)) {
            return;
        }

        if (v == null) {
            return;
        } else if (v == mBtnLogo) {
            mActivity.showSwitchModeDialog();
        } else if (v == mViewAddGroup) {
            DFAddNewGroup dlg = new DFAddNewGroup();
            dlg.setAddNewGroupListener(this);
            dlg.showHelper(getActivity());
        }
    }

    public class GroupListAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;

        public GroupListAdapter() {
            mLayoutInflater = LayoutInflater.from(mActivity);
        }

        @Override
        public int getCount() {
            return mGroupList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return mGroupList.get(position).group_Id;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rootView = convertView;

            GroupHolder viewholder;
            if (rootView == null) {
                rootView = mLayoutInflater.inflate(R.layout.listitem_groups, null);
                viewholder = new GroupHolder(rootView);
                rootView.setTag(viewholder);
            } else {
                viewholder = (GroupHolder) rootView.getTag();
            }

            final GroupData group = mGroupList.get(position);

            viewholder.textGroupName.setText(group.name);

            viewholder.rbSelect.setChecked(group.group_Id==mSharedPref.getShowedGroupId());
            viewholder.rbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UtilDBG.d("[RYAN] isChecked : " + isChecked);

                    if (isChecked) {
                        mSharedPref.setShowedGroupId(group.group_Id);
                        notifyDataSetChanged();
                    }
                }
            });

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
            dlg.setTitle(getString(R.string.dlg_delete_group_title));
            dlg.setDes(getString(R.string.dlg_delete_group_des));
            dlg.setPositiveButton(null, new DlgMessageYN.btnHandler() {
                @Override
                public boolean onBtnHandler() {
//                    GroupData group = mGroupList.get(position);
//                    boolean success = mPD.deleteGroup(group.group_Id);
//                    if (success) {
//                        mGroupList = mPD.getListAllGroup();
//                        mAdapter.notifyDataSetChanged();
//                    }
                    return true;
                }
            }).showHelper(mActivity);
        }
    }



    private class GroupHolder {
        public TextView textGroupName;
        public RadioButton rbSelect;
        public View viewDelete;

        public GroupHolder(View view) {
            textGroupName = (TextView) view.findViewById(R.id.text_group_name);
            rbSelect = (RadioButton) view.findViewById(R.id.rb_select);
            viewDelete = view.findViewById(R.id.bottom_delete);
        }
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (MXWApp.isClickFast(view)) {
                return;
            }

            DFGroupInfo dlg = new DFGroupInfo();
            dlg.setGroupData(mGroupList.get(position));
            dlg.showHelper(mActivity);
        }
    };
}
