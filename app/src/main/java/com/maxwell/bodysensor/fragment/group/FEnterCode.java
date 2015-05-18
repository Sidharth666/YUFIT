package com.maxwell.bodysensor.fragment.group;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.maxwell.bodysensor.GroupActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.dialogfragment.group.DFAddNewGroup;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by ryanhsueh on 15/3/17.
 */
public class FEnterCode extends Fragment implements View.OnClickListener {

    public static final String CODE_WISTRON = "5643";

    private GroupActivity mActivity;

    private DFAddNewGroup mDFAddNewGroup;

    private EditText mEditCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UtilDBG.logMethod();
        return inflater.inflate(R.layout.fragment_enter_code, null, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = (GroupActivity) getActivity();

        View rootView = getView();

        mEditCode = (EditText) rootView.findViewById(R.id.edit_membership_code);

        rootView.findViewById(R.id.btnCancel).setOnClickListener(this);
        rootView.findViewById(R.id.btnOK).setOnClickListener(this);
    }

    public void setDFAddNewGroup(DFAddNewGroup f) {
        mDFAddNewGroup = f;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel :
                mDFAddNewGroup.typeBack();
                break;
            case R.id.btnOK :
//                String code = mEditCode.getText().toString();
//                if (code.equals(CODE_WISTRON)) {
                    mDFAddNewGroup.setupGroup();
//                } else {
//                    WarningUtil.showToastLong(mActivity, "Your membership code is incorrect!");
//                }

                break;
        }
    }
}
