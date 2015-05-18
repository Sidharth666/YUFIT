package com.maxwell.bodysensor.dialogfragment;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.sos.SOSRecord;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilLocale;
import com.maxwell.bodysensor.util.UtilTime;

import java.util.Date;
import java.util.List;

/**
 * Created by ryanhsueh on 15/4/23.
 */
public class DFSOSHistory extends DFBase {

    private DBProgramData mPD;

    private ListView mLvSOSHistory;
    private SOSHistoryAdapter mAdapter;

    private List<SOSRecord> mListSOSRecord;

    @Override
    public String getDialogTag() {
        return MainActivity.DF_SOS_HISTORY;
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

        mPD = DBProgramData.getInstance();

        View view = inflater.inflate(R.layout.df_sos_history, null);

        mListSOSRecord = mPD.getAllSOSRecords();
        mAdapter = new SOSHistoryAdapter();
        mLvSOSHistory = (ListView) view.findViewById(R.id.list_sos_history);
        mLvSOSHistory.setAdapter(mAdapter);

        setupTitleText(view, R.string.profile_sos_history);
        setupButtons(view);

        UtilDBG.i("[RYAN] DFSOSHistory > onCreateView > size : " + mListSOSRecord.size());

        return view;
    }

    public class SOSHistoryAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;

        public SOSHistoryAdapter() {
            mLayoutInflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {
            return mListSOSRecord.size();
        }

        @Override
        public SOSRecord getItem(int position) {
            return mListSOSRecord.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView = convertView;

            SOSViewHolder viewholder;
            if (rootView == null) {
                rootView = mLayoutInflater.inflate(R.layout.listitem_sos_history, null);
                viewholder = new SOSViewHolder(rootView);
                rootView.setTag(viewholder);
            } else {
                viewholder = (SOSViewHolder) rootView.getTag();
            }

            SOSRecord sos = mListSOSRecord.get(position);

            Date date = new Date(UtilTime.getMillisecond(sos.dateUnixTime));
            viewholder.textSOSDate.setText(UtilLocale.dateToString(date, UtilLocale.DateFmt.YMDHMa));

            viewholder.textSOSDescription.setText(
                    Html.fromHtml(String.format(getString(R.string.sos_history_sent_des),
                            sos.contactName)));

            viewholder.textSOSLink.setText(Html.fromHtml(String.format(MainActivity.SOS_HISTORY_LOCATION_LINK,
                    sos.latitude, sos.longitude, sos.latitude, sos.longitude)));
            viewholder.textSOSLink.setMovementMethod(LinkMovementMethod.getInstance());

            viewholder.textSOSLocation.setText(
                    String.format(getString(R.string.sos_location),
                    sos.latitude, sos.longitude));

            return rootView;
        }
    }

    public class SOSViewHolder {
        public TextView textSOSDate;
        public TextView textSOSDescription;
        public TextView textSOSLink;
        public TextView textSOSLocation;

        public SOSViewHolder(View view) {
            textSOSDate = (TextView) view.findViewById(R.id.text_sos_date);
            textSOSDescription = (TextView) view.findViewById(R.id.text_sos_description);
            textSOSLink = (TextView) view.findViewById(R.id.text_sos_link);
            textSOSLocation = (TextView) view.findViewById(R.id.text_sos_location);
        }
    }
}
