package com.maxwell.bodysensor.dialogfragment;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.maxwell.bodysensor.MXWApp;
import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DeviceData;
import com.maxwell.bodysensor.util.UtilDBG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Date;

/**
 * Created by ryanhsueh on 15/2/11.
 */
public class DFAbout extends DFBase implements View.OnClickListener {

    private final int COUNT_SHOW_DEVICE_INFO = 5;

    private DBProgramData mPD;

    private int mCountForDeviceInfo = 0;

    @Override
    public String getDialogTag() {
        return MainActivity.DF_ABOUT;
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

        View view = inflater.inflate(R.layout.df_about, container);

        view.findViewById(R.id.img_logo).setOnClickListener(this);

        TextView link = (TextView) view.findViewById(R.id.yu_play_god);

        link.setMovementMethod(LinkMovementMethod.getInstance());

        // get app version name
        PackageInfo pInfo = null;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (pInfo!=null) {
//            textVerion.setText(pInfo.versionName);
        } else  {
            UtilDBG.e("try get PackageInfo, but fail");
        }

        setupTitleText(view, R.string.fcAbout);
        setupButtons(view);
        hideButtonOK();

        return view;
    }

    private void exportFile(String strSourceFolder, String strFilename) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            String dbPath = "/storage/sdcard0/BackupFolder";

            if (sd.canWrite()) {

                File folder = new File(dbPath);
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdir();
                }

                if (success) {
                    String  currentDBPath = strSourceFolder + strFilename;
                    String backupDBPath  = "/" + strFilename;
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(dbPath, backupDBPath);

                    FileInputStream fis = new FileInputStream(currentDB);
                    FileOutputStream fos = new FileOutputStream(backupDB);
                    FileChannel src = fis.getChannel();
                    FileChannel dst = fos.getChannel();
                    dst.transferFrom(src, 0, src.size());

                    src.close();
                    dst.close();
                    fis.close();
                    fos.close();
                    if (UtilDBG.isDebuggable()) {
                        Toast.makeText(getActivity(), backupDB.toString(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (UtilDBG.isDebuggable()) {
                        Toast.makeText(getActivity(), "Folder " + dbPath + " is not existed", Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
//        if (MXWApp.isClickFast(v)) {
//            return;
//        }

        switch (v.getId()) {
            case R.id.img_logo:

                mCountForDeviceInfo++;

                if (mCountForDeviceInfo >= COUNT_SHOW_DEVICE_INFO) {
                    if (UtilDBG.isDebuggable()) {
                        UtilDBG.e("user press the version info");
                        String packageName = getActivity().getApplicationContext().getPackageName();
                        exportFile("/data/" + packageName +"/databases/", "info.db");
                        exportFile("/data/" + packageName +"/databases/", "bs.db");
                        exportFile("/data/" + packageName +"/shared_prefs/", "pref_key.xml");
                    }

                    //++ show device detail
                    DeviceData data = mPD.getUserDeviceByAddress(mPD.getTargetDeviceMac());
                    if (data == null) {
                        return;
                    } else {
                        MXWApp app = (MXWApp) getActivity().getApplication();
                        StringBuilder sb= new StringBuilder();
                        sb.append(" <<<< Device Information >>>> \n");
                        sb.append("FW revision:     " + app.getDevFWRevision() + "\n");
                        sb.append("HW revision:     " + app.getDevHWRevision() + "\n");
                        sb.append("SW revision:     " + app.getDevSWRevision() + "\n");
                        sb.append("Serial Number:     " + app.getDevSerialNumber() + "\n\n");
                        sb.append("last daily sync time:     " + new Date(data.lastDailySyncTime*1000) + "\n");
                        sb.append("last hourly sync time:     " + new Date(data.lastHourlySyncTime*1000) + "\n");
                        sb.append("last timezone diff: " + data.lastTimezoneDiff + "\n");
                        sb.append("display name:       " + data.displayName + "\n");
                        sb.append("address:            " + data.mac + "\n");

                        DFBlank blank = new DFBlank();
                        blank.setContent("ECDetail", sb.toString());
                        blank.showHelper(getActivity());
                    }
                    //-- show device detail
                }

                break;
        }
    }
}
