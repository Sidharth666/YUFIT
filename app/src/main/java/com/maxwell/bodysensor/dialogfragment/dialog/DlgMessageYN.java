package com.maxwell.bodysensor.dialogfragment.dialog;

import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.maxwell.bodysensor.dialogfragment.DFBase;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;

public class DlgMessageYN extends DFBase implements
    View.OnClickListener {

    private TextView mTextTitle;
    private TextView mTextDes;
    private Button mBtnCancel;
    private Button mBtnOK;

    private String mStringTitle = null;
    private String mStringDes = null;
    private String mStringCancel = null;
    private String mStringOK = null;

    private boolean mBtnCancelEnable;
    private boolean mBtnOKEnable;

    // return true, to close DlgMessageYN; otherwise, return false;
    public interface btnHandler {
        boolean onBtnHandler();
    }

    private btnHandler mHandlerCancel;
    private btnHandler mHandlerOK;

    @Override
    public String getDialogTag() {
        return MainActivity.DLG_MESSAGE_YN;
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

        View view = inflater.inflate(R.layout.dlg_message_yn, container);

        mTextTitle = (TextView) view.findViewById(R.id.ynTitle);
        mTextDes = (TextView) view.findViewById(R.id.ynDescription);
        mBtnOK = (Button) view.findViewById(R.id.btnOK);
        mBtnCancel = (Button) view.findViewById(R.id.btnCancel);

        if (mStringTitle!=null) {
            mTextTitle.setText(mStringTitle);
        }
        if (mStringDes!=null) {
            mTextDes.setText(mStringDes);
        }
        if (mStringOK!=null) {
            mBtnOK.setText(mStringOK);
        }
        if (mStringCancel!=null) {
            mBtnCancel.setText(mStringCancel);
        }

        mBtnOK.setOnClickListener(this);
        mBtnOK.setEnabled(mBtnOKEnable);
        mBtnCancel.setOnClickListener(this);
        mBtnCancel.setEnabled(mBtnCancelEnable);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Ref. How to make DialogFragment width to ....
        // http://stackoverflow.com/questions/23990726/how-to-make-dialogfragment-width-to-fill-parent
        if (getDialog() != null && getDialog().getWindow()!=null) {
            getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }

    public DlgMessageYN() {
        super();

        mBtnCancelEnable = true;
        mBtnOKEnable = true;
    }

    public DlgMessageYN setTitle(String title) {
        mStringTitle = title;
        return this;
    }

    public DlgMessageYN setDes(String description) {
        mStringDes = description;
        return this;
    }

    public DlgMessageYN setPositiveButton(String text, btnHandler h) {
        if (text!=null) {
            mStringOK = text;
        }
        mHandlerOK = h;
        return this;
    }

    public DlgMessageYN setNegativeButton(String text, btnHandler h) {
        if (text!=null) {
            mStringCancel = text;
        }
        mHandlerCancel = h;
        return this;
    }

    public DlgMessageYN setButtonEnabled(boolean ok, boolean cancel) {
        mBtnOKEnable = ok;
        mBtnCancelEnable = ok;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v==mBtnOK) {
            if (mHandlerOK!=null) {
                if (mHandlerOK.onBtnHandler()) {
                    dismissIt();
                }
            } else {
                dismissIt();
            }
        } else if (v==mBtnCancel) {
            if (mHandlerCancel!=null) {
                if (mHandlerCancel.onBtnHandler()) {
                    dismissIt();
                }
            } else {
                dismissIt();
            }
        } else {
            UtilDBG.e("DlgMessageYN, unexpected onClick()");
        }
    }

    private void dismissIt() {
        if (getDialog() != null) {
            getDialog().dismiss();
        }
    }
}
