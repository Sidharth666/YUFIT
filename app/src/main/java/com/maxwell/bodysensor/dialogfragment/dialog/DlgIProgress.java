package com.maxwell.bodysensor.dialogfragment.dialog;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxwell.bodysensor.dialogfragment.DFBase;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;

// indeterminate progress
public class DlgIProgress extends DFBase {
    private ImageView mImgRotate;
    private TextView mTextDescription;
    private String mStrDescription;

    @Override
    public String getDialogTag() {
        return MainActivity.DLG_I_PROGRESS;
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

        View view = inflater.inflate(R.layout.dlg_iprogress, container);

        mImgRotate = (ImageView) view.findViewById(R.id.iprgImage);
        mTextDescription = (TextView) view.findViewById(R.id.iprgText);
        mTextDescription.setText(mStrDescription);

        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getActivity(), R.animator.animator_rotate);
        anim.setTarget(mImgRotate);

        anim.start();

        setCancelable(false); // call  DialogFragment 's setCancelable();

        return view;
    }

    public void setDescription(String string) {
        mStrDescription = string;
        if (mTextDescription!=null) {
            mTextDescription.setText(mStrDescription);
        }
    }
}
