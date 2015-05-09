package com.maxwell.bodysensor.dialogfragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.DBDevice;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.user.DBUserDevice;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgMessageYN;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;
import com.maxwell.bodysensor.data.DeviceData;
import com.maxwell.bodysensor.util.UtilDBG;

import java.util.List;

/**
 * Created by ryanhsueh on 15/2/2.
 */
public class DFDeviceList extends DFBase implements AdapterView.OnItemClickListener,
        DBDevice.OnDBDeviceUpdateListener {

    private MainActivity mActivity;
    private MGActivityTrackerApi mMaxwellBLE;
    private DBProgramData mPD;
    private DBDevice mDevManager;

    private ListView mLvDevice;

    private Button mBtnEdit;

    private List<DeviceData> mDeviceList;
    private DeviceListAdapter mAdapter;

    private boolean mEnableDelete = false;

    @Override
    public String getDialogTag() {
        return MainActivity.DF_DEVICE_LIST;
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

        mActivity = (MainActivity) getActivity();
        mMaxwellBLE = MGActivityTracker.getInstance(mActivity);
        mPD = DBProgramData.getInstance();

        mDevManager = DBUserDevice.getInstance();
        mDevManager.addListener(this);

        View view = inflater.inflate(R.layout.df_device_list, container);

        // get all device list
        mDeviceList = mPD.getDeviceList();
        mAdapter = new DeviceListAdapter();
        mLvDevice = (ListView) view.findViewById(R.id.list_device);
        mLvDevice.setAdapter(mAdapter);
        mLvDevice.setOnItemClickListener(this);

        mBtnEdit = (Button) view.findViewById(R.id.btn_edit);
        mBtnEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mEnableDelete) {
                    mBtnEdit.setText(R.string.edit);
                } else {
                    mBtnEdit.setText(R.string.strDone);
                }

                mEnableDelete = !mEnableDelete;
                mAdapter.notifyDataSetChanged();
            }
        });

        setupTitleText(view, R.string.fcDeviceList);
        setupButtons(view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mDevManager.removeListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DFDeviceInfo dlg = new DFDeviceInfo();
        dlg.setDeviceData(mDeviceList.get(position));
        dlg.showHelper(mActivity);
    }

    @Override
    public void OnDBDeviceUpdated() {
        mDeviceList = mPD.getDeviceList();

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public class DeviceListAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;

        public DeviceListAdapter() {
            mLayoutInflater = LayoutInflater.from(mActivity);
        }

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rootView = convertView;

            DeviceViewHolder viewholder = null;
            if (rootView == null) {
                rootView = mLayoutInflater.inflate(R.layout.listitem_device, null);
                viewholder = new DeviceViewHolder(rootView);
                rootView.setTag(viewholder);
            } else {
                viewholder = (DeviceViewHolder) rootView.getTag();
            }

            DeviceData device = mDeviceList.get(position);

            viewholder.textDevicName.setText(device.displayName);
            viewholder.textDevicMac.setText(device.mac);

            if (!mEnableDelete) {
                viewholder.btnDelete.setVisibility(View.GONE);
            } else {
                viewholder.btnDelete.setVisibility(View.VISIBLE);
                viewholder.btnDelete.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        DlgMessageYN dlg = new DlgMessageYN();
                        dlg.setTitle(getString(R.string.device_delete_title))
                                .setDes(getString(R.string.device_delete_description))
                                .setPositiveButton(null, new DlgMessageYN.btnHandler() {

                                    @Override
                                    public boolean onBtnHandler() {
                                        final String mac_deleting = mDeviceList.get(position).mac;

                                        // If the device you want to delete is focus device now, disconnect it!!
                                        String focusMac = mPD.getTargetDeviceMac();
                                        if (focusMac.equalsIgnoreCase(mac_deleting)) {
                                            mMaxwellBLE.disconnect();
                                        }

                                        mPD.removeUserDevice(mac_deleting);
                                        return true;
                                    }

                                });
                        dlg.showHelper(mActivity);
                    }
                });
            }

            return rootView;
        }
    }

    public class DeviceViewHolder {
        public TextView textDevicName;
        public TextView textDevicMac;
        public ImageView btnDelete;

        public DeviceViewHolder(View view) {
            textDevicName = (TextView) view.findViewById(R.id.text_device_name);
            textDevicMac = (TextView) view.findViewById(R.id.text_device_mac);
            btnDelete = (ImageView) view.findViewById(R.id.imgb_delete);
        }
    }

}