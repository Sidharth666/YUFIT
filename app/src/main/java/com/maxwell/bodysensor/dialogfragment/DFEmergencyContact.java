package com.maxwell.bodysensor.dialogfragment;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.SharedPrefWrapper;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTracker;
import com.maxwellguider.bluetooth.activitytracker.MGActivityTrackerApi;

/**
 * Created by ryanhsueh on 15/4/23.
 */
public class DFEmergencyContact extends DFBase {

    private SharedPrefWrapper mSharedPref;
    private MGActivityTrackerApi mMaxwellBLE;

    private EditText mEditName;
    private EditText mEditEmail;
    private EditText mEditPhoneNumber;

    private TextView mTextSOSDescription;

    @Override
    public String getDialogTag() {
        return MainActivity.DF_EMERGENCY_CONTACT;
    }

    @Override
    public int getDialogTheme() {
        return R.style.app_df_trans_rr;
    }

    @Override
    public void saveData() {
        String name = getEditText(mEditName, "");
        String email = getEditText(mEditEmail, "");
        String phoneNumber = getEditText(mEditPhoneNumber, "");

        mSharedPref.saveEmergencyContactName(name);
        mSharedPref.saveEmergencyContactEmail(email);
        mSharedPref.saveEmergencyContactPhone(phoneNumber);

        boolean enableSOS = !phoneNumber.equals("");
        mMaxwellBLE.enableSOS(enableSOS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UtilDBG.logMethod();

        mSharedPref = SharedPrefWrapper.getInstance();
        mMaxwellBLE = MGActivityTracker.getInstance(getActivity());

        View view = inflater.inflate(R.layout.df_emergency_contact, container);

        mEditName = (EditText) view.findViewById(R.id.edit_contact_name);
        mEditEmail = (EditText) view.findViewById(R.id.edit_contact_email);
        mEditPhoneNumber = (EditText) view.findViewById(R.id.edit_contact_phone_no);
        mTextSOSDescription = (TextView) view.findViewById(R.id.text_sos_description);

        mEditName.setText(mSharedPref.getEmergencyContactName());
        mEditEmail.setText(mSharedPref.getEmergencyContactEmail());
        mEditPhoneNumber.setText(mSharedPref.getEmergencyContactPhone());
        mTextSOSDescription.setText(Html.fromHtml(getString(R.string.sos_description)));

        setupTitleText(view, R.string.profile_sos_contact);
        setupButtons(view);

        return view;
    }

    private String getEditText(EditText edt, String strDefault) {
        String str = edt.getText().toString();
        if (str.length()>0) {
            return str;
        }
        return strDefault;
    }

}
