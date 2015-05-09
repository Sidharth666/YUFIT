package com.maxwell.bodysensor.dialogfragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;

public class DFBlank extends DFBase {
    String mStrTitle;
    String mStrText;
    TextView mTitle;
    TextView mText;

    @Override
    public String getDialogTag() {
        return MainActivity.DF_BLANK;
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

        View view = inflater.inflate(R.layout.df_blank, container);

        mTitle = (TextView) view.findViewById(R.id.text_title_bar);
        mText = (TextView) view.findViewById(R.id.blank_text);

        setupButtons(view);

        if (mStrTitle!=null) {
            mTitle.setText(mStrTitle);
        }
        if (mStrText!=null) {
            mText.setText(mStrText);
        }

        return view;
    }

    public void setContent(String strTitle, String strText) {
        mStrTitle = strTitle;
        mStrText = strText;
    }
}
