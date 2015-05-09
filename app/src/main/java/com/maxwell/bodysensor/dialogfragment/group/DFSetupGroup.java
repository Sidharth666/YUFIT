package com.maxwell.bodysensor.dialogfragment.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.maxwell.bodysensor.GroupActivity;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.SharedPrefWrapper;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.group.GroupData;
import com.maxwell.bodysensor.dialogfragment.DFBase;
import com.maxwell.bodysensor.ui.ViewLinearLayout;
import com.maxwell.bodysensor.ui.WarningUtil;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwellguider.bluetooth.activitytracker.GoalType;

/**
 * Created by ryanhsueh on 15/3/27.
 */
public class DFSetupGroup extends DFBase implements View.OnClickListener {

    private GroupActivity mActivity;
    private SharedPrefWrapper mSharedPref;
    private DBProgramData mPD;

    private DFAddNewGroup mDFAddNewGroup;

    private ViewLinearLayout mLayoutRoot;

    private EditText mEditName;
    private EditText mEditSchool;
    private EditText mEditClass;

    private RadioGroup mRGGoalType;
    private EditText mEditEnergyGoal;
    private EditText mEditStepGoal;

    private GoalType mGoalType = GoalType.STEP;

    private GroupData mGroup;

    @Override
    public String getDialogTag() {
        return GroupActivity.DF_SETUP_GROUP;
    }

    @Override
    public int getDialogTheme() {
        return R.style.app_df_trans_rr;
    }

    @Override
    public void saveData() {
        String name = getEditText(mEditName, "");
        String school = getEditText(mEditSchool, "");
        String _class = getEditText(mEditClass, "");

        int goal = 0;

        String str = getGoalString();
        if (str.length()>0) {
            goal = Integer.parseInt(str);
        }

        mGroup.name = name;
        mGroup.school = school;
        mGroup._class = _class;
        mGroup.daily_goal = goal;
        mGroup.goal_type = mGoalType;

        if (mDFAddNewGroup == null) {
            mPD.updateGroup(mGroup);
        } else {
            mDFAddNewGroup.setupGroupFinsh(mGroup);
        }
    }

    @Override
    public boolean checkData() {
        String str;
        int i = 0;

        str = getEditText(mEditName, "");
        if (str.equals("")) {
            WarningUtil.showToastLong(mActivity, R.string.enter_your_group_name);
            return false;
        }

        str = getGoalString();
        if (str.length()>0) {
            i = Integer.parseInt(str);
        }

        if (i<=0) {
            WarningUtil.showDFMessageOK(getActivity(), 0, R.string.goal_error_no_value);
            return false;
        }

        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UtilDBG.logMethod();
        return inflater.inflate(R.layout.df_setup_group, null, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = (GroupActivity) getActivity();
        mSharedPref = SharedPrefWrapper.getInstance();
        mPD = DBProgramData.getInstance();

        View rootView = getView();

        mLayoutRoot = (ViewLinearLayout) rootView.findViewById(R.id.layout_root);
        mLayoutRoot.setHideInputNonEditing(true, this);

        mEditName = (EditText) rootView.findViewById(R.id.edit_group_name);
        mEditSchool = (EditText) rootView.findViewById(R.id.edit_group_school);
        mEditClass = (EditText) rootView.findViewById(R.id.edit_group_class);

        mRGGoalType = (RadioGroup) rootView.findViewById(R.id.rg_goal_type);
        mEditEnergyGoal = (EditText) rootView.findViewById(R.id.edit_energy_goal);
        mEditStepGoal = (EditText) rootView.findViewById(R.id.edit_step_goal);

        if (mGroup == null) { // new group
            mGroup = new GroupData();

            int goal = mPD.getPersonGoal();
            mEditEnergyGoal.setText(String.valueOf(goal));
            mEditStepGoal.setText(String.valueOf(goal));

            mGoalType = mSharedPref.getDeviceGoalType();
        } else {
            mEditName.setText(mGroup.name);
            mEditSchool.setText(mGroup.school);
            mEditClass.setText(mGroup._class);
            mEditEnergyGoal.setText(String.valueOf(mGroup.daily_goal));
            mEditStepGoal.setText(String.valueOf(mGroup.daily_goal));
        }

        updateView(mGoalType);
        setupButtons(rootView);
        setupTitleText(rootView, R.string.setup_groups);

        mRGGoalType.setOnCheckedChangeListener(mGoalTypeCheckListener);

        mEditEnergyGoal.setSelection(mEditEnergyGoal.getText().toString().length());
        mEditStepGoal.setSelection(mEditStepGoal.getText().toString().length());

        mDismissAfterOKCancel = (mDFAddNewGroup==null);

        if (!mDismissAfterOKCancel) {
            rootView.findViewById(R.id.btnCancel).setOnClickListener(this);
        }



    }

    private void updateView(GoalType goalType) {
        if (goalType == GoalType.ENERGY) {
            mRGGoalType.check(R.id.rb_energy_goal);
            mEditEnergyGoal.setVisibility(View.VISIBLE);
            mEditStepGoal.setVisibility(View.INVISIBLE);
        } else {
            mRGGoalType.check(R.id.rb_step_goal);
            mEditEnergyGoal.setVisibility(View.INVISIBLE);
            mEditStepGoal.setVisibility(View.VISIBLE);
        }
    }

    public void setGroupData(GroupData group) {
        mGroup = group;
    }

    public void setDFAddNewGroup(DFAddNewGroup f) {
        mDFAddNewGroup = f;
    }

    private String getEditText(EditText edt, String strDefault) {
        String str = edt.getText().toString();
        if (str.length()>0) {
            return str;
        }
        return strDefault;
    }
    private String getGoalString() {
        String str = "";
        if (mGoalType == GoalType.ENERGY) {
            str = mEditEnergyGoal.getText().toString();
        } else {
            str = mEditStepGoal.getText().toString();
        }

        return str;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel :
                mDFAddNewGroup.typeBack();
                break;
        }
    }

    private RadioGroup.OnCheckedChangeListener mGoalTypeCheckListener =
            new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    switch (checkedId) {
                        case R.id.rb_energy_goal:
                            mGoalType = GoalType.ENERGY;
                            break;
                        case R.id.rb_step_goal:
                            mGoalType = GoalType.STEP;
                            break;
                    }

                    updateView(mGoalType);
                }

            };
}
