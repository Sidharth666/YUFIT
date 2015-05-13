package com.maxwell.bodysensor.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maxwell.bodysensor.MXWActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.dialogfragment.DFAddNewDevice;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgIProgress;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgMessageYN;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgMessageYN.btnHandler;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwellguider.bluetooth.AdvertisingData;

import java.util.ArrayList;
import java.util.List;

public class FAddSearch extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

	private MXWActivity mActivity;

    private DFAddNewDevice mDFAddNew = null;

    private ProgressBar pb;
    private ListView mListView;
    private DeviceAdapter mAdapter;

    private DlgIProgress mDlgProgress;

    private List<AdvertisingData> mDeviceList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        UtilDBG.logMethod();

        mActivity = (MXWActivity) getActivity();

        View view = inflater.inflate(R.layout.add_search, null, false);

        mDeviceList = new ArrayList<AdvertisingData>();
        mListView = (ListView) view.findViewById(R.id.list_devices);
        mAdapter = new DeviceAdapter(mActivity);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        view.findViewById(R.id.imgb_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_pair_again).setOnClickListener(this);

        return view;
    }

    public void setDFAddNew(DFAddNewDevice df) {
        mDFAddNew = df;
    }

    public void addScanDevice(final AdvertisingData newDevice) {
        boolean needUpdate = true;
        for (AdvertisingData device : mDeviceList) {
            if (device.address.equalsIgnoreCase(newDevice.address)) {
                needUpdate = false;
                break;
            }
        }

        if (needUpdate) {
            mDeviceList.add(newDevice);
            updateDeviceListView();
        }
    }

    private void updateDeviceListView() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void pairDevice(final AdvertisingData device) {
    	DlgMessageYN dlg = new DlgMessageYN();
    	if (device.pairCode == null) {
            connectDevice(device);
    	} else {
    		dlg.setDes("Pair Code : " + device.pairCode);
        	dlg.setPositiveButton(null, new btnHandler() {

    			@Override
    			public boolean onBtnHandler() {
                    connectDevice(device);
    				return true;
    			}

        	});

        	dlg.showHelper(mActivity);
    	}
    }

    private void connectDevice(final AdvertisingData device) {
        showPairProgress();
        mDFAddNew.setTargetDevice(device);
    }

    private void showPairProgress() {
    	mDlgProgress = new DlgIProgress();
		mDlgProgress.setDescription(getString(R.string.ipPairing));
		mDlgProgress.showHelper(mActivity);
    }

    public void pairFinished() {
    	if (mDlgProgress != null) {
            mDlgProgress.dismiss();
    	}
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final AdvertisingData device = mDeviceList.get(position);

        // TODO : need to fix for P07
        pairDevice(device);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.imgb_cancel:
                if (mDFAddNew != null) {
                    mDFAddNew.page20Back();
                }
                break;
            case R.id.btn_pair_again:
//                mBleWrapper.pairAllForNew();
                break;
        }
    }

    private class DeviceAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public DeviceAdapter(Context context) {
            inflater = LayoutInflater.from(context);
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
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView = convertView;

            DeviceViewHolder viewholder;
            if (rootView == null) {
                rootView = inflater.inflate(R.layout.listitem_search_device, null);
                viewholder = new DeviceViewHolder(rootView);
                rootView.setTag(viewholder);
            } else {
                viewholder = (DeviceViewHolder) rootView.getTag();
            }

            final AdvertisingData device = mDeviceList.get(position);

            viewholder.textDevicName.setText(device.deviceName);
            viewholder.textPairCode.setText("Pair code: " + device.pairCode);

            return rootView;
        }
    }

    public class DeviceViewHolder {
        public TextView textDevicName;
        public TextView textPairCode;

        public DeviceViewHolder(View view) {
            textDevicName = (TextView) view.findViewById(R.id.text_device_name);
            textPairCode = (TextView) view.findViewById(R.id.text_pair_code);
        }
    }
}
