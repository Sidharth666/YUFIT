package com.maxwell.bodysensor.dialogfragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.maxwell.bodysensor.MXWActivity;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DeviceData;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by ryanhsueh on 15/2/2.
 */
public class DFDeviceInfo extends DFBase {

    private MXWActivity mActivity;
    private DBProgramData mPD;

    private DFAddNewDevice mDFAddNew = null;

    private DeviceData mDevcieData;

    private EditText mEditDeviceName;
    private TextView mTextDeviceMac;

    @Override
    public String getDialogTag() {
        return MainActivity.DF_DEVICE_INFO;
    }

    @Override
    public int getDialogTheme() {
        return R.style.app_df_trans_rr;
    }

    @Override
    public void saveData() {
        boolean isUserMode = (mActivity instanceof MainActivity);

        String devName = mEditDeviceName.getText().toString();
        mDevcieData.displayName = devName;

        if (isUserMode) {
            // User Mode
            mPD.updateUserDeviceData(mDevcieData);
        } else {
            // Group Mode
            mPD.updateGroupDeviceData(mDevcieData);
        }


        if (mDFAddNew != null) {
            mDFAddNew.finishEditDevice();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UtilDBG.logMethod();

        mActivity = (MXWActivity) getActivity();
        mPD = DBProgramData.getInstance();

        View view = inflater.inflate(R.layout.df_device_info, null);

        mEditDeviceName = (EditText) view.findViewById(R.id.edit_device_name);
        mTextDeviceMac = (TextView) view.findViewById(R.id.text_device_mac);

        updateView();

        setupTitleText(view, R.string.fcSectionDevice);
        setupButtons(view);

        mDismissAfterOKCancel = (mDFAddNew==null);
        if (mDFAddNew != null) {
            hideButtonCancel();
        }

        return view;
    }

    public void setDFAddNew(DFAddNewDevice df) {
        mDFAddNew = df;
    }

    public void setDeviceData(DeviceData device) {
        mDevcieData = device;
    }

    public void updateView() {
        if (mDevcieData != null) {
            mEditDeviceName.setText(mDevcieData.displayName);
            mTextDeviceMac.setText(mDevcieData.mac);
        }
    }

}
