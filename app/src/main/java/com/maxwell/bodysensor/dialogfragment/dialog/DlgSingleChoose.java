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
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.dialogfragment.DFBase;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by ryanhsueh on 15/3/25.
 */
public class DlgSingleChoose extends DFBase implements
        View.OnClickListener {

    private ListView mListView;

    private String mStringOK = null;

    private String[] mDataArray;
    private int mIndex;

    // return true, to close Dlg; otherwise, return false;
    public interface btnHandler {
        boolean onBtnHandler();
    }

    private btnHandler mHandlerOK;

    @Override
    public String getDialogTag() {
        return MXWActivity.DLG_SINGLE_CHOOSE;
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

        View view = inflater.inflate(R.layout.dlg_single_choose, container);

        mBtnOK = (Button) view.findViewById(R.id.btnOK);

        if (mStringOK!=null) {
            mBtnOK.setText(mStringOK);
        }

        mBtnOK.setOnClickListener(this);

        initDeviceList(view);

        return view;
    }

    private void initDeviceList(View view) {
        if (mDataArray != null && mDataArray.length > 0) {
            mListView = (ListView) view.findViewById(R.id.list_single_choose);
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

    public void setSelected(int index) {
        mIndex = index;
    }

    public int getSelected() {
        return mIndex;
    }

    public void setListData(String[] dataArray) {
        mDataArray = dataArray;
    }

    private void dismissIt() {
        if (getDialog() != null) {
            getDialog().dismiss();
        }
    }

    public DlgSingleChoose setPositiveButton(String text, btnHandler h) {
        if (text!=null) {
            mStringOK = text;
        }
        mHandlerOK = h;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v==mBtnOK) {
            if (mHandlerOK != null) {
                mHandlerOK.onBtnHandler();
            }
            dismissIt();
        } else {
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
            return mDataArray.length;
        }

        @Override
        public String getItem(int position) {
            return mDataArray[position];
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

            ((TextView) convertView.findViewById(R.id.text_name)).setText(mDataArray[position]);

            return convertView;
        }

    }
}
