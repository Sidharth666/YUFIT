package com.maxwell.bodysensor.dialogfragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.util.UtilConst;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by ryanhsueh on 15/4/14.
 */
public class DFInfo extends DFBase {

    private TextView mTextInfo;
    private ImageView mImageDesc;

    private String mStrInfo;
    private int mTitleResId = UtilConst.INVALID_INT;
    private int mDescImage;

    @Override
    public String getDialogTag() {
        return MainActivity.DF_INFO;
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

        View view = inflater.inflate(R.layout.df_info, null);

        mTextInfo = (TextView) view.findViewById(R.id.text_info);
        mImageDesc = (ImageView) view.findViewById(R.id.desc_icon);

        if (mStrInfo != null) {
            mTextInfo.setText(mStrInfo);
        }
        if (mTitleResId != UtilConst.INVALID_INT) {
            setupTitleText(view, mTitleResId);
        }
        if(mImageDesc != null){
            mImageDesc.setBackgroundResource(mDescImage);
        }

        setupButtons(view);

        return view;
    }

    public void setTitleResId(int resId) {
        mTitleResId = resId;
    }

    public void setInfo(String info) {
        mStrInfo = info;
    }

    public void setImg(int resId){
        mDescImage = resId;
    }
}
