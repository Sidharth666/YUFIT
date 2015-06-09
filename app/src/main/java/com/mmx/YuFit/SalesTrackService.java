package com.mmx.YuFit;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.maxwell.bodysensor.SharedPrefWrapper;
import com.maxwell.bodysensor.util.UtilDBG;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * An {@link android.app.IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class SalesTrackService extends IntentService {

    String uri;
    String deviceIdImei;
    String currentTime;
    String networkInfo;
    String tabletRegs;
    String macId;
    protected SharedPrefWrapper mSharedPref;


    public SalesTrackService() {
        super("SalesTrackService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if(intent!=null){
            macId = intent.getExtras().getString("MacId").replace(":","");
        }

        mSharedPref = SharedPrefWrapper.getInstance();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileNwInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNwInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if ((wifiNwInfo.isAvailable() || mobileNwInfo.isAvailable())&& (mobileNwInfo.isConnected() || wifiNwInfo.isConnected())) {

            //get current time
             currentTime = getCurrentTime();
            //get device imei
             deviceIdImei = getImei();
            //get network info
             networkInfo = getSignalInfo();

            if(networkInfo.equals("Network not found")){
                networkInfo = "000000:0000:0000";
            }

            tabletRegs = tabletReg(deviceIdImei, networkInfo);
            uri = "http://sts.micromaxinfo.com/configureSms/msg.aspx?tim="+ currentTime+ "&Msg="+ tabletRegs;
            httpMessageSend(uri.toString().replace(" ",""));

        }
    }


    public void httpMessageSend(final String gtMessage) {
        try {
            URL url = new URL(gtMessage);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            String Ack = readStream(in);

            if (Ack.contains("OK")) {
                UtilDBG.e("SalesTrackService, Ackowledgement success" + Ack);
                getSharedPreferences("locValues", 0).edit().putString("Serverloc", "message sent wifi").commit();
                mSharedPref.setSalesTrackStatus(true);
                /*Intent i = new Intent("com.package.MY_DIALOG");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);*/
//                stopSelf();
            } else {
                UtilDBG.e("SalesTrackService, Ackowledgement fail" + Ack);
            }
            urlConnection.disconnect();

        } catch (Exception e) {
            UtilDBG.e("SalesTrackService, Exception" + e);
        }

    }

    private String readStream(InputStream in) {
        String total = "";
        try {

            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String x;
            x = r.readLine();
            while (x != null) {
                total += x;
                x = r.readLine();
            }
        } catch (Exception ex) {

        }
        return total;
    }

    public String tabletReg(String serial_no, String netInfo){


        String softwareVersion = null;
        String regMessageChecksum;
        try {

            if (!Build.DISPLAY.equals(null)) {
                softwareVersion = Build.DISPLAY;
            } 
        } catch (Exception e) {
            // TODO: handle BuildNumber Exception
            UtilDBG.e("SalesTrackService, Exception" + e);

        }

        String netArray[] = netInfo.split(":");

        /***
         * @author :
         *
         */
        String regMessage = "REG:01:01" + netArray[0] + ":02" + netArray[1]+ ":03" + netArray[2] + ":04" + macId + ":05"+ macId + ":06" + "V1.0" + ":07" + softwareVersion + ":";

        String checkSum = checkSumGenerator(regMessage);
        regMessageChecksum = "REG:01:01" + netArray[0] + ":02" + netArray[1]+ ":03" + netArray[2] + ":04" + macId + ":05"+ macId + ":06" + "V1.0" + ":07" + softwareVersion + ":"+ checkSum + ":";

        return regMessageChecksum;

    }

    public String checkSumGenerator(String s) {
        int sum = 0;
        for (int i = 0; i < s.length(); i++) {

            sum = sum + (int) s.charAt(i);

        }
        int mod;
        mod = sum % 255;

        String hex = Integer.toHexString(mod).toUpperCase();

        return hex;
    }

    public String getCurrentTime(){
        Date systemDates = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        return simpleDateFormat.format(systemDates);


    }

    public String getImei(){

        TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getDeviceId();
    }

    public String getSignalInfo() {
        String netInfo = null;
        int cellPadding;
        String lacId_hex = null;
        String cellId_hex = null;
        int mcc = 0, mnc = 0;
        int cid = 0, lac = 0;

        TelephonyManager telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));

        // getting Network mcc-mnc
        String networkOp = telephonyManager.getNetworkOperator();

        if (networkOp.length() != 0) {
            mcc = Integer.parseInt(networkOp.substring(0, 3));
            mnc = Integer.parseInt(networkOp.substring(3));
        }

        int networkType = telephonyManager.getNetworkType();

        if (networkType == TelephonyManager.NETWORK_TYPE_UMTS) {
            cellPadding = 8;
        } else {
            cellPadding = 4;
        }



            GsmCellLocation loc = (GsmCellLocation) telephonyManager.getCellLocation();
            if (loc != null) {
                cid = loc.getCid();
                lac = loc.getLac();

                lacId_hex = getPaddedHex(lac, 4);
                cellId_hex = getPaddedHex(cid, cellPadding);
            } else {

                cid = 0;
                lac = 0;

                lacId_hex = getPaddedHex(lac, 4);
                cellId_hex = getPaddedHex(cid, cellPadding);

            }


        if (mcc != 0 && mnc != 0 && cid != 0 & lac != 0)

        {
            netInfo = mcc + "" + mnc + ":" + cellId_hex + ":" + lacId_hex;

        } else {
            netInfo = "Network not found";
        }

        UtilDBG.e("SalesTrackService, network signal data: " + netInfo);

        return netInfo;

    }

    String getPaddedHex(int nr, int minLen) {
        String str = Integer.toHexString(nr).toUpperCase();
        if (str != null) {
            while (str.length() < minLen) {
                str = "0" + str;
            }
        }
        return str;
    }


}
