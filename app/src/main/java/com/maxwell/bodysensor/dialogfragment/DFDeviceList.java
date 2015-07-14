package com.maxwell.bodysensor.dialogfragment;

import android.content.Intent;
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

    //private Button mBtnEdit;
    private Button mBtnRemove;
    private Button mBtnRename;


    private List<DeviceData> mDeviceList;
    private DeviceListAdapter mAdapter;

    //private boolean mEnableDelete = false;

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

        //Replace listview with textview
      /* mDeviceName=(TextView)view.findViewById(R.id.text_device_name);
       mDeviceName.setText(mDeviceList.get(0).displayName);
       mDeviceMacId=(TextView)view.findViewById(R.id.text_device_mac);
       mDeviceMacId.setText(mDeviceList.get(0).mac);*/
        mAdapter = new DeviceListAdapter();
        mLvDevice = (ListView) view.findViewById(R.id.list_device);
        mLvDevice.setAdapter(mAdapter);
        mLvDevice.setOnItemClickListener(this);
        //Remove Click
        mBtnRemove = (Button)view.findViewById(R.id.remove);
        mBtnRemove.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DlgMessageYN dlg = new DlgMessageYN();
                dlg.setTitle(getString(R.string.device_delete_title))
                        .setDes(getString(R.string.device_delete_description))
                        .setPositiveButton(null, new DlgMessageYN.btnHandler() {

                            @Override
                            public boolean onBtnHandler() {
                              /*  final String mac_deleting = mDeviceList.get(0).mac;

                                // If the device you want to delete is focus device now, disconnect it!!
                                String focusMac = mPD.getTargetDeviceMac();
                                if (focusMac.equalsIgnoreCase(mac_deleting)) {
                                    mMaxwellBLE.disconnect();
                                }*/
                                removeDevice(mDeviceList.get(0).mac);
                                //mPD.removeUserDevice(mac_deleting);
                                mBtnRemove.setVisibility(View.GONE);
                                mBtnRename.setVisibility(View.GONE);
                                //send Broadcast to HM indicating device deletion
                                Intent intent = new Intent();
                                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                intent.setAction("com.healthifyme.mmx.ACTION_DEVICE_REMOVED");
                                intent.putExtra("MacId", mPD.getTargetDeviceMac());
                                getActivity().sendBroadcast(intent);
                                getDialog().dismiss();
                                return true;
                            }

                        });
                dlg.showHelper(mActivity);
                mAdapter.notifyDataSetChanged();
            }
        });
        //Rename Click
        mBtnRename = (Button)view.findViewById(R.id.rename);
        mBtnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DFDeviceInfo dlg = new DFDeviceInfo();
                dlg.setDeviceData(mDeviceList.get(0));
                dlg.showHelper(mActivity);
            }
        });
        setupTitleText(view, R.string.fcDeviceSettings);
        setupButtons(view);

        return view;
    }
    private void removeDevice(String address) {
        // If the device you want to delete is focus device now, disconnect it!!
        String currentMac = mPD.getTargetDeviceMac();
        if (currentMac.equalsIgnoreCase(address)) {
            mMaxwellBLE.disconnect();
        }
        mPD.removeUserDevice(address);
        mPD.removeUserProfile(address);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mDevManager.removeListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*DFDeviceInfo dlg = new DFDeviceInfo();
        dlg.setDeviceData(mDeviceList.get(position));
        dlg.showHelper(mActivity);*/
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