package com.maxwell.bodysensor.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.dialogfragment.DFAddNewDevice;
import com.maxwell.bodysensor.util.UtilDBG;

public class FAddSelectType extends Fragment implements View.OnClickListener {
    DFAddNewDevice mDFAddNew = null;
    boolean mIsFirstLaunch = false;

    Button mBtnBack, btnOk;
    View mViewP07a = null;
    View mViewNone = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        UtilDBG.logMethod();

        View view = inflater.inflate(R.layout.add_select_type, null, false);

        TextView textTitle = (TextView) view.findViewById(R.id.text_title_bar);
        mBtnBack = (Button) view.findViewById(R.id.btnCancel);
        btnOk = (Button) view.findViewById(R.id.btnOK);
        btnOk.setVisibility(View.INVISIBLE);
        mViewP07a = view.findViewById(R.id.view_pair_device);
        mViewNone = view.findViewById(R.id.view_skip_pair);

        mBtnBack.setOnClickListener(this);
        mViewP07a.setOnClickListener(this);
        mViewNone.setOnClickListener(this);

        textTitle.setText(R.string.pair_my_capsule);

        if (!mIsFirstLaunch) {
            mViewNone.setVisibility(View.GONE);
        }

        return view;
    }

    public void setDFAddNew(DFAddNewDevice df) {
        mDFAddNew = df;
    }

    public void setIsFirstLaunch(boolean b) {
        mIsFirstLaunch = b;
    }

    @Override
    public void onClick(View v) {
        if (v==mBtnBack) {
            if (mDFAddNew!=null) {
                mDFAddNew.cancelPairDevice();
            }
        } else if (v==mViewP07a) {
            if (mDFAddNew!=null) {
                mDFAddNew.page30Next();
            }
        } else if (v==mViewNone) {
            if (mDFAddNew!=null) {
                mDFAddNew.skipPairDevice();
            }
        }
    }
}
