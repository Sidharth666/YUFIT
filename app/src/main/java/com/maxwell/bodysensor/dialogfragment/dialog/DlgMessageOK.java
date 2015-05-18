package com.maxwell.bodysensor.dialogfragment.dialog;


import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maxwell.bodysensor.dialogfragment.DFBase;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;

public class DlgMessageOK extends DFBase {
    private TextView mTextTitle;
    private TextView mTextDes;

    private String mTitle = null;
    private List<String> mListDes;

    @Override
    public String getDialogTag() {
        return MainActivity.DLG_MESSAGE_OK;
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

        View view = inflater.inflate(R.layout.dlg_message_ok, container);

        mTextTitle = (TextView) view.findViewById(R.id.ynTitle);
        mTextDes = (TextView) view.findViewById(R.id.ynDescription);

        setupButtons(view);

        if (mTitle == null) {
        	mTextTitle.setText(getResources().getString(R.string.app_name));
        } else {
        	mTextTitle.setText(mTitle);
        }

        if (mListDes == null || mListDes.size() <=0) {
            UtilDBG.e("DlgMessageOK.onCreateView, what string do you want to show?");
        } else {
            if (mListDes.size()==1) {
                mTextDes.setText(mListDes.get(0));
            } else {
                String strDes = "";
                int i = 0;
                for (String string : mListDes) {
                    strDes += Integer.toString(++i);
                    strDes += ". ";
                    strDes += string;
                    strDes += "\n";
                }
                mTextDes.setText(strDes);
            }
        }

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

    public DlgMessageOK() {
        super();

        mListDes = new ArrayList<String>();
    }

    public DlgMessageOK setTitle(String title) {
        mTitle = title;
        return this;
    }

    public DlgMessageOK addDesString(String description) {
        if (mListDes == null) {
            UtilDBG.e("DlgMessageOK.addDesString, the list is null");
            return this;
        }

        mListDes.add(description);
        return this;
    }
}
