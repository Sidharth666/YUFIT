package com.maxwell.bodysensor.dialogfragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.MXWApp;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;

public abstract class DFBase extends DialogFragment {

    protected Button mBtnCancel;
    protected Button mBtnOK;
    protected ImageButton mBtnCancelImg;
    protected boolean mDismissAfterOKCancel = true;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_TITLE, getDialogTheme());
        Dialog dlg = super.onCreateDialog(savedInstanceState);
        dlg.setCanceledOnTouchOutside(false);

        return dlg;
    }

    @Override
    public void onDestroy() {
        dialogClosing(getDialogTag());

        super.onDestroy();
    }

    // every derived dialog has its own dialog tag string
    public abstract String getDialogTag();
    // every derived dialog, to define its own dialog theme style
    public abstract int getDialogTheme();

    // if necessary, save data when closing dialog
    public abstract void saveData();

    public boolean checkData() {
        return true;
    }

    // setup title bar text
    protected void setupTitleText(View view, int resId) {
        TextView title = (TextView) view.findViewById(R.id.text_title_bar);
        if (title != null) {
            title.setText(resId);
        }
    }
    protected void setupTitleText(View view, String text) {
        TextView title = (TextView) view.findViewById(R.id.text_title_bar);
        if (title != null) {
            title.setText(text);
        }
    }

    // setup the button (cancel, ok..), note that the id need to be same as the assumed one
    protected void setupButtons(View view) {
        mBtnCancel = (Button) view.findViewById(R.id.btnCancel);
        mBtnOK = (Button) view.findViewById(R.id.btnOK);
        mBtnCancelImg = (ImageButton) view.findViewById(R.id.btnCancelImg);
        if (mBtnCancel != null) {
            mBtnCancel.setOnClickListener(mCancelClickListener);
        }
        if (mBtnOK != null) {
            mBtnOK.setOnClickListener(mOKClickListener);
        }
        if (mBtnCancelImg != null) {
            mBtnCancelImg.setOnClickListener(mCancelClickListener);
        }
    }

    protected void hideButtonOK() {
        if (mBtnOK != null) {
            mBtnOK.setVisibility(View.INVISIBLE);
        }
    }
    protected void hideButtonCancel() {
        if (mBtnCancel != null) {
            mBtnCancel.setVisibility(View.INVISIBLE);
        }
        if (mBtnCancelImg != null) {
            mBtnCancelImg.setVisibility(View.INVISIBLE);
        }
    }

    public void showHelper(FragmentActivity activity) {
        String tag = getDialogTag();
        if (tag == null) {
            return ;
        }

        if (activity==null) {
            return ;
        }

        show(activity.getSupportFragmentManager(), tag);
    }

    private void doSaveData() {
        saveData();
        dialogSaving(getDialogTag());
    }

    // the listener for clicking cancel button
    OnClickListener mCancelClickListener =  new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (MXWApp.isClickFast(v)) {
                return;
            }

            if (mDismissAfterOKCancel) {
            	if (getDialog() != null) {
            		getDialog().dismiss();
            	}
            }
        }
    };

    // the listener for clicking OK button
    OnClickListener mOKClickListener =  new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (MXWApp.isClickFast(v)) {
                return;
            }

            if (!checkData()) {
                return;
            }

            doSaveData();

            if (mDismissAfterOKCancel) {
                getDialog().dismiss();
            }
        }
    };

    // two methods for printing logs
    // dialogClosing is always been called
    // dialogSaving is only called when the data is updated, or data saved

    private void dialogClosing(String tag) {
        UtilDBG.i("dialogClosing()   dlgID = " + tag);
    }

    private void dialogSaving(String tag) {
        UtilDBG.i("dialogSaving()----dlgID = " + tag);
    }
}
