package com.maxwell.bodysensor.dialogfragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by ryanhsueh on 15/4/14.
 */
public class DFPhoneConnection extends DFBase {

    @Override
    public String getDialogTag() {
        return MainActivity.DF_PHONE_CONNECTION;
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

        View view = inflater.inflate(R.layout.df_phone_connection, null);

        setupTitleText(view, R.string.phone_connection);
        setupButtons(view);

        return view;
    }

}
