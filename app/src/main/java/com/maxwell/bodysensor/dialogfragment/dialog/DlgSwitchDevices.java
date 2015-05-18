package com.maxwell.bodysensor.dialogfragment.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.maxwell.bodysensor.MXWApp;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.dialogfragment.DFBase;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;
import com.maxwell.bodysensor.data.DeviceData;
import com.maxwell.bodysensor.util.UtilDBG;

import java.util.List;

/**
 * Created by ryanhsueh on 15/2/3.
 */
public class DlgSwitchDevices extends DFBase implements
        View.OnClickListener {

    private MGActivityTrackerApi mMaxwellBLE;
    private DBProgramData mPD;

    private TextView mTextTitle;
    private Button mBtnCancel;
    private Button mBtnOK;
    private ListView mListView;

    private String mStringTitle = null;
    private String mStringCancel = null;
    private String mStringOK = null;

    private String mFocusMacAddress = null;

    List<DeviceData> mDeviceList;
    private int mIndex;

    // return true, to close DlgMessageYN; otherwise, return false;
    public interface btnHandler {
        boolean onBtnHandler();
    }

    private btnHandler mHandlerOK;

    @Override
    public String getDialogTag() {
        return MainActivity.DLG_SWITCH_DEVICES;
    }

    @Override
    public int getDialogTheme() {
        return R.style.app_dlg_scale_tt;
    }

    @Override
    public void saveData() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UtilDBG.logMethod();

        mMaxwellBLE = MGActivityTracker.getInstance(getActivity());
        mPD = DBProgramData.getInstance();

        View view = inflater.inflate(R.layout.dlg_switch_devices, container);

        mTextTitle = (TextView) view.findViewById(R.id.ynTitle);
        mBtnOK = (Button) view.findViewById(R.id.btnOK);
        mBtnCancel = (Button) view.findViewById(R.id.btnCancel);

        if (mStringTitle!=null) {
            mTextTitle.setText(mStringTitle);
        }
        if (mStringOK!=null) {
            mBtnOK.setText(mStringOK);
        }
        if (mStringCancel!=null) {
            mBtnCancel.setText(mStringCancel);
        }

        mBtnOK.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        initDeviceList(view);

        return view;
    }

    private void initDeviceList(View view) {
        mDeviceList = mPD.getDeviceList();

        if (mDeviceList != null && mDeviceList.size() > 0) {
            mFocusMacAddress = mPD.getTargetDeviceMac();
            mIndex = 0;
            for (DeviceData device : mDeviceList) {
                if (mFocusMacAddress.equalsIgnoreCase(device.mac)) {
                    break;
                }
                mIndex++;
            }

            mListView = (ListView) view.findViewById(R.id.list_switch_devices);
            mListView.setAdapter(new SingleListAdapter());
            mListView.setItemChecked(mIndex, true);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position,
                                        long id) {
                    mIndex = position;
                }
            });
        }
    }

    public DlgSwitchDevices setPositiveButton(String text, btnHandler h) {
        if (text!=null) {
            mStringOK = text;
        }
        mHandlerOK = h;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v==mBtnOK) {

            if (mDeviceList.size() > 0) {
                DeviceData device = mDeviceList.get(mIndex);
                String newTarget = device.mac;

                // update user focus device & connect it
                String targetMac = mPD.getTargetDeviceMac();
                if (!targetMac.equalsIgnoreCase(newTarget)) {
                    if (MXWApp.isPowerWatch(newTarget)) {
                        mPD.setTargetDeviceMac(newTarget);
                        mMaxwellBLE.connect(newTarget, 0);
                    }
                }
            }

            if (mHandlerOK!=null) {
                if (mHandlerOK.onBtnHandler()) {
                    dismissIt();
                }
            } else {
                dismissIt();
            }
        } else if (v==mBtnCancel) {
            dismissIt();
        } else {
            UtilDBG.e("DlgMessageYN, unexpected onClick()");
        }
    }

    private void dismissIt() {
        if (getDialog() != null) {
            getDialog().dismiss();
        }
    }

    /*
     * Issue :
     * 		Android customized single choice ListView
     *
     * solution :
     * 		http://stackoverflow.com/questions/19812809/custom-listview-with-single-choice-selection
     */
    protected class SingleListAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;

        public SingleListAdapter() {
            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public DeviceData getItem(int position) {
            return mDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listitem_switch_devices, null);
            }

            DeviceData device = mDeviceList.get(position);
            ((TextView) convertView.findViewById(R.id.text_name)).setText(device.displayName);

            return convertView;
        }

    }
}
