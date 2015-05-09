package com.maxwell.bodysensor.fragment;

import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.dialogfragment.DFAddNewDevice;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FAdd60 extends Fragment implements View.OnClickListener {
    DFAddNewDevice mDFAddNew = null;
    View mViewNext = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        UtilDBG.logMethod();

        View view = inflater.inflate(R.layout.add_60, null, false);

        mViewNext = view.findViewById(R.id.next);
        mViewNext.setOnClickListener(this);

        return view;
    }

    public void setDFAddNew(DFAddNewDevice df) {
        mDFAddNew = df;
    }

    @Override
    public void onClick(View v) {
        if (v==mViewNext) {
            if (mDFAddNew!=null) {
                mDFAddNew.goToEditDevice();
            }
        }
    }
}
