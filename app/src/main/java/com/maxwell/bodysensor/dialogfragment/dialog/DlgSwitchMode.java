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

import com.maxwell.bodysensor.MXWActivity;
import com.maxwell.bodysensor.MXWApp;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DeviceData;
import com.maxwell.bodysensor.dialogfragment.DFBase;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwellguider.bluetooth.MGPeripheral.DeviceType;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;

import java.util.List;

/**
 * Created by ryanhsueh on 15/3/17.
 */
public class DlgSwitchMode extends DFBase implements
        View.OnClickListener {

    private MGActivityTrackerApi mMaxwellBLE;
    private DBProgramData mPD;

    private View mViewDismiss;
    private Button mBtnSwitch;
    private ListView mListView;

    private String mStringSwitch = null;

    private String mTargetDeviceMac = null;

    List<DeviceData> mDeviceList;
    private int mIndex;

    // return true, to close DlgMessageYN; otherwise, return false;
    public interface btnHandler {
        boolean onBtnHandler();
    }

    private btnHandler mHandlerSwitchMode;
    private btnHandler mHandlerSwitchDevice;

    @Override
    public String getDialogTag() {
        return MXWActivity.DLG_SWITCH_MODE;
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

        MXWActivity activity = (MXWActivity) getActivity();
        mMaxwellBLE = MGActivityTracker.getInstance(activity);
        mPD = DBProgramData.getInstance();

        View view = inflater.inflate(R.layout.dlg_switch_mode, container);

        mViewDismiss = view.findViewById(R.id.layout_dismiss);

        mBtnSwitch = (Button) view.findViewById(R.id.btn_switch_mode);
        if (mStringSwitch!=null) {
            mBtnSwitch.setText(mStringSwitch);
        }

        mViewDismiss.setOnClickListener(this);
        mBtnSwitch.setOnClickListener(this);

        mListView = (ListView) view.findViewById(R.id.list_switch_devices);

        if (activity instanceof MainActivity) {
            initDeviceList();
        } else {
            mListView.setVisibility(View.GONE);
        }

        return view;
    }

    private void initDeviceList() {
        mDeviceList = mPD.getDeviceList();

        if (mDeviceList != null && mDeviceList.size() > 0) {
            mTargetDeviceMac = mPD.getTargetDeviceMac();
            mIndex = 0;
            for (DeviceData device : mDeviceList) {
                if (mTargetDeviceMac.equalsIgnoreCase(device.mac)) {
                    break;
                }
                mIndex++;
            }

            mListView.setAdapter(new SingleListAdapter());
            mListView.setItemChecked(mIndex, true);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position,
                                        long id) {
                    DeviceData device = mDeviceList.get(position);
                    String newTargetMac = device.mac;
                    String currentMac = mPD.getTargetDeviceMac();

                    // update user focus device & connect it
                    if (!currentMac.equalsIgnoreCase(newTargetMac)) {
                        if (MXWApp.isPowerWatch(newTargetMac)) {
                            mPD.setTargetDeviceMac(newTargetMac);
                            mMaxwellBLE.connect(newTargetMac, 0);
                        }
                    }

                    if (mHandlerSwitchDevice != null) {
                        mHandlerSwitchDevice.onBtnHandler();
                    }

                    dismissIt();
                }
            });
        } else {
            mListView.setVisibility(View.GONE);
        }
    }

    public DlgSwitchMode setSwitchDeviceButton(btnHandler h) {
        mHandlerSwitchDevice = h;
        return this;
    }

    public DlgSwitchMode setSwitchModeButton(String text, btnHandler h) {
        if (text!=null) {
            mStringSwitch = text;
        }
        mHandlerSwitchMode = h;
        return this;
    }

    private void dismissIt() {
        if (getDialog() != null) {
            getDialog().dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        UtilDBG.d("DlgSwitchMode, onClick !!!!!!!!!!!!!");

        if (v==mBtnSwitch) {
            if (mHandlerSwitchMode != null) {
                if (mHandlerSwitchMode.onBtnHandler()) {
                    dismissIt();
                }
            } else {
                dismissIt();
            }
//        } else if (v==mViewDismiss) {
//            dismissIt();
        } else {
            dismissIt();
            UtilDBG.e("DlgMessageYN, unexpected onClick()");
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
