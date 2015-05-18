package com.maxwell.bodysensor;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.telephony.SmsManager;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;
import com.maxwell.bodysensor.data.DBDevice;
import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DBProvider;
import com.maxwell.bodysensor.data.DBUpgradeWrapper;
import com.maxwell.bodysensor.data.DBUtils;
import com.maxwell.bodysensor.data.DeviceData;
import com.maxwell.bodysensor.data.PrimaryProfileData;
import com.maxwell.bodysensor.data.UserModeType;
import com.maxwell.bodysensor.data.sos.SOSRecord;
import com.maxwell.bodysensor.data.user.DBUser15MinutesRecord;
import com.maxwell.bodysensor.data.user.DBUserDailyRecord;
import com.maxwell.bodysensor.data.user.DBUserHourlyRecord;
import com.maxwell.bodysensor.data.user.DBUserProfile;
import com.maxwell.bodysensor.data.user.DBUserSleepLog;
import com.maxwell.bodysensor.data.user.DBUserSleepScore;
import com.maxwell.bodysensor.fragment.FTabConf;
import com.maxwell.bodysensor.listener.OnActivityResultCallback;
import com.maxwell.bodysensor.listener.OnFitnessUpdateListener;
import com.maxwell.bodysensor.ui.WarningUtil;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilConst;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilDate;
import com.maxwell.bodysensor.util.UtilExceptionHandler;
import com.maxwell.bodysensor.util.UtilLocale;
import com.maxwell.bodysensor.util.UtilLocale.DateFmt;
import com.maxwell.bodysensor.util.UtilTZ;
import com.maxwell.bodysensor.util.UtilTime;
import com.maxwellguider.bluetooth.AdvertisingData;
import com.maxwellguider.bluetooth.MGPeripheral;
import com.maxwellguider.bluetooth.activitytracker.FitnessType;
import com.maxwellguider.bluetooth.activitytracker.LanguageType;
import com.maxwellguider.bluetooth.activitytracker.UnitType;
import com.maxwellguider.bluetooth.command.AttributeValue;
import com.maxwellguider.bluetooth.command.feature.AttributeType;
import com.mmx.YuFit.SalesTrackService;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/*
 * Model
 * - DBProgramData
 * - - DBHelper
 * View
 * - MainActivity
 * - - DFXxx DlgXxxx (DialogFragment), FXxx (Fragment), ViewXXX (View)
 * Other
 * - ECInfo, UtilXxx
 * */

public class MainActivity extends MXWActivity implements
        DBUpgradeWrapper.DBUpgradeListener,
        LocationListener {

    public static final String SOS_HISTORY_LOCATION_LINK = "<a href=\"https://www.google.com/maps/place/%1$s+%2$s/@%3$s,%4$s,15z\">My location on Google Map</a>";

    private final static int NOTIFICATION_FIND_PHONE = 500;

    public static final int DELAY_STOP_ALERT = 10000; // ms

    // Used to calculate calories from distance.
    public static final double CALORIES_PER_KM_PER_KG = 0.75;

    public static final String FB_NAME_SPACE = "bodysensor";
    public static final String FB_AVATAR_FILE_NAME = "facebook_avatar.png";

    // containers
    public static final String CONTAINER_FIRST = "ContainerFirst";
    public static final String CONTAINER_MAIN = "ContainerMain";
    public static final String CONTAINER_SETTINGS = "ContainerSettings";

    // tabs
    public static final String TAB_SPEC_HOME = "HOME";
    public static final String TAB_SPEC_TREND = "TREND";
    public static final String TAB_SPEC_PLAYGROUND = "PLAYGROUND";
    public static final String TAB_SPEC_SETTING = "SETTING";

    // tab, trends
    public static final String TAG_TREND_A = "trend_a";
    public static final String TAG_TREND_SD = "trend_s_d";
    public static final String TAG_TREND_SNOND = "trend_s_non_d";


    //settings
    private FTabConf mContainerSettings;

    // Warning Dialog
    private Dialog mDlgAlert;

    // Warning Ringtone
    private Ringtone mRingtoneFindPhone;

    private boolean mIsFirstStart = false;

    private boolean mNeedUpgradDB = false;
    private ProgressDialog mDlgProgress;

    private LocationManager mLocationMgr;

    private boolean mIsOnResumed = false;

    private static final String AUTHORITY = UtilConst.HME_PACKAGE_NAME + ".providers.authority.MXX_PROVIDER";
    public static final Uri CONTENT_URI_DBPROFILE = Uri.parse("content://" + AUTHORITY + "/DBProfile");
    public static final Uri CONTENT_URI_DB_DEVICE = Uri.parse("content://" + AUTHORITY + "/DBDevice");

    public static final Uri CONTENT_URI_DB_HOURLYREC = Uri.parse("content://" + AUTHORITY + "/DBHourlyRecord");
    public static final Uri CONTENT_URI_DB_DAILYREC = Uri.parse("content://" + AUTHORITY + "/DBDailyRecord");
    public static final Uri CONTENT_URI_DB_15MINREC = Uri.parse("content://" + AUTHORITY + "/DB15MinutesRecord");
    public static final Uri CONTENT_URI_DB_SLEEPLOG = Uri.parse("content://" + AUTHORITY + "/DBSleepLog");
    public static final Uri CONTENT_URI_DB_SLEEPSCORE = Uri.parse("content://" + AUTHORITY + "/DBDailySleepScoreRecord");

    private PrimaryProfileData mPrimaryProfile;
    private String macId;

    private CopyDataAsync copyAsync;

    private boolean isFromHM = false;
    public static final String KEY_FROM_HME_APP = "is_from_hme";


    private OnActivityResultCallback mActivityResultCallback;

    public void setOnActivityResultListener(OnActivityResultCallback listener) {
        mActivityResultCallback = listener;
    }

    private OnFitnessUpdateListener mFitnessUpdateListener;

    public void setOnFitnessMoveListener(OnFitnessUpdateListener listener) {
        mFitnessUpdateListener = listener;
    }

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, DBProvider.DBUSER15MINTABLE, 1);
        uriMatcher.addURI(AUTHORITY, DBProvider.DBUSERDAILYTABLE, 2);
        uriMatcher.addURI(AUTHORITY, DBProvider.DBUSERDEVICETABLE, 3);
        uriMatcher.addURI(AUTHORITY, DBProvider.DBUSERHOURLYTABLE, 4);
        uriMatcher.addURI(AUTHORITY, DBProvider.DBUSERPROFILETABLE, 5);
        uriMatcher.addURI(AUTHORITY, DBProvider.DBUSERSLEEPLOGTABLE, 6);
        uriMatcher.addURI(AUTHORITY, DBProvider.DBUSER1SLEEPSCORETABLE, 7);

    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };

    private static final boolean ENABLE_HM_COPY = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Uncaught Exception
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        UtilExceptionHandler eh = new UtilExceptionHandler(pInfo != null ? pInfo.versionName : "version name unknown");

        if (UtilDBG.isDebuggable()) {
            // Save to local DB
            Thread.setDefaultUncaughtExceptionHandler(eh);
        } else {
            // Use Crashlytics, https://www.crashlytics.com/
            Crashlytics.start(this);
        }

        // build time
        String strBuildTime = "";
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            zf.close();
            UtilCalendar cal = new UtilCalendar(time / 1000, UtilTZ.getDefaultTZ());
            strBuildTime = UtilLocale.calToString(cal, DateFmt.YMDHMa);
        } catch (Exception e) {
        }

        // apk install time
        String strInstallTime = "";
        if (pInfo != null) {
            UtilCalendar cal = new UtilCalendar(pInfo.lastUpdateTime / 1000, UtilTZ.getDefaultTZ());
            strInstallTime = UtilLocale.calToString(cal, DateFmt.YMDHMa);
        }

        enableBluetooth();

        // Shared Preference
        mSharedPref = SharedPrefWrapper.getInstance();

        // program data, database
        mPD = DBProgramData.getInstance();


        // BLE api
        initMaxwellBleApi();


        //check for HM package
        if (UtilConst.isHMPackageInstalled(this) && ENABLE_HM_COPY && mPD.getTargetDeviceMac().equals("")) {
            //DB code here

            copyAsync = new CopyDataAsync();
            macId = updateUserProfileHM();
            if (!macId.equals("")) {
                WarningUtil.showToastLong(this, "Please Wait..Fetching Data");
                copyAsync.execute();
            }
        } else {
            String address = mPD.getTargetDeviceMac();
            if (StringUtils.isNotEmpty(address)) {
                if (MXWApp.initBleAutoConnection(address)) {
                    MXWApp.connectDevice(address);
                }
            }
        }

        // calendar, locale
        UtilCalendar calNow = new UtilCalendar(null);
        Date date = new Date();

        int minuteOffset = UtilTZ.getDefaultRawOffset();

        Date dateUTC = UtilDate.getUTCdatetimeAsDate();

        UtilLocale.init(getResources());
        Locale localeDefault = UtilLocale.getDefaultLocale();
        Locale localeApp = UtilLocale.getAppLocale();

        UtilDBG.i("===============================================");
        UtilDBG.i("<<<<<<<<<<<<    MainActivity     >>>>>>>>>>>>>>");
        UtilDBG.i("===============================================");
        UtilDBG.i(eh.getDeviceInfo());
        UtilDBG.i(eh.getOSInfo());
        UtilDBG.i(eh.getSoftwareInfo());
        UtilDBG.i("[apk] build time:   " + strBuildTime);
        UtilDBG.i("[apk] install time: " + strInstallTime);
        UtilDBG.i("default time zone | display:" + UtilTZ.getDefaultTZ().getDisplayName() + " | offset (minutes): " + UtilTZ.getDefaultRawOffset());
        UtilDBG.i("current time (local : UtilCalendar): " + UtilLocale.calToString(calNow, DateFmt.YMDHMa));
        UtilDBG.i("current time (local : Date): " + UtilLocale.dateToString(date, DateFmt.YMDHMa));
        UtilDBG.i("current time (UTC : Date): " + UtilLocale.dateToString(dateUTC, DateFmt.YMDHMa));
        UtilDBG.i("default locale | display:" + localeDefault.getDisplayName() + " | language:" + localeDefault.getLanguage() + ", " + localeDefault.getCountry());
        UtilDBG.i("app locale | display:" + localeApp.getDisplayName() + " | language:" + localeApp.getLanguage() + ", " + localeApp.getCountry());
        UtilDBG.i("===============================================");

        UtilDBG.i("===============================================");
        switch (ProductFlavors.sType) {
            case Maxwell:
                UtilDBG.i("[apk] Product -> Maxwell");
                break;
            case Micromax:
                UtilDBG.i("[apk] Product -> Micromax");
                break;
            default:
                UtilDBG.i("[apk] Product -> Maxwell");
                break;
        }
        UtilDBG.i("===============================================");

        setContentView(R.layout.activity_main);

        restoreFragments();

        checkFirstStart();

        // Set up ringtone of finding phone.
        mRingtoneFindPhone = RingtoneManager.getRingtone(MainActivity.this,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

        // register SmartKeyReceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MXWApp.ACTION_SMART_KEY_FIND_PHONE);
        intentFilter.addAction(MXWApp.ACTION_SOS);
        registerReceiver(mDeviceEventReceiver, intentFilter);

        // check intent action to do something
        checkIntentAction();
    }

    private void enableBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    private void copyHMData() {
//        String macId = updateUserProfileHM();
        UtilDBG.i("MainActivity, Fetch data from HM ");
        Uri[] tables = {CONTENT_URI_DB_15MINREC, CONTENT_URI_DB_DAILYREC, CONTENT_URI_DB_DEVICE, CONTENT_URI_DB_HOURLYREC,
                CONTENT_URI_DBPROFILE, CONTENT_URI_DB_SLEEPLOG, CONTENT_URI_DB_SLEEPSCORE};
        for (Uri item : tables) {
            Cursor cursor = updateDBDataHM(item);
            try {
                if (DBUtils.isCursorUsable(cursor)) {
                    UtilDBG.i("MainActivity, Fetch data from HM ,Cursor: " + cursor);
                    switch (uriMatcher.match(item)) {
                        case 1:
                            DBUser15MinutesRecord.getInstance().update15MinRecord(cursor);
                            break;
                        case 2:
                            DBUserDailyRecord.getInstance().updateDailyRecord(cursor);
                            break;
                        case 3:
                            updateUserDeviceData(macId);
                            break;
                        case 4:
                            DBUserHourlyRecord.getInstance().updateHourlyRecord(cursor);
                            break;
                        case 5:
//                        updateUserProfileHM();
                            break;
                        case 6:
                            DBUserSleepLog.getInstance().updateSleepLogRecord(cursor);
                            break;
                        case 7:
                            DBUserSleepScore.getInstance().updateSleepScoreRecord(cursor);
                            break;

                    }
                }
            }finally {
                DBUtils.closeCursor(cursor);
            }
        }
    }


    private Cursor updateDBDataHM(Uri uri) {
        Cursor c = getContentResolver().query(uri, null, null, null, null);
        return c;
    }


    private String updateUserProfileHM() {
        String address = "";
        mPrimaryProfile = PrimaryProfileData.getInstace();
        Cursor c1 = getContentResolver().query(CONTENT_URI_DBPROFILE, DBUserProfile.PROJECTION, null, null, null);
        try {
            if (DBUtils.isCursorUsable(c1)) {
                c1.moveToFirst();
                address = c1.getString(c1.getColumnIndex(DBUserProfile.COLUMN.DEVICE_MAC));
                mPrimaryProfile._Id = c1.getLong(c1.getColumnIndex(DBUserProfile.COLUMN._ID));
                mPrimaryProfile.name = c1.getString(c1.getColumnIndex(DBUserProfile.COLUMN.NAME));
                mPrimaryProfile.gender = c1.getInt(c1.getColumnIndex(DBUserProfile.COLUMN.GENDER));
                mPrimaryProfile.birthday = c1.getLong(c1.getColumnIndex(DBUserProfile.COLUMN.BIRTHDAY));
                mPrimaryProfile.height = c1.getDouble(c1.getColumnIndex(DBUserProfile.COLUMN.HEIGHT));
                mPrimaryProfile.weight = c1.getDouble(c1.getColumnIndex(DBUserProfile.COLUMN.WEIGHT));
                mPrimaryProfile.stride = c1.getDouble(c1.getColumnIndex(DBUserProfile.COLUMN.STRIDE));
                mPrimaryProfile.photo = c1.getBlob(c1.getColumnIndex(DBUserProfile.COLUMN.PHOTO));
                mPrimaryProfile.dailyGoal = c1.getInt(c1.getColumnIndex(DBUserProfile.COLUMN.DAILY_GOAL));
                mPrimaryProfile.sleepLogBegin = c1.getInt(c1.getColumnIndex(DBUserProfile.COLUMN.SLEEP_LOG_BEGIN));
                mPrimaryProfile.sleepLogEnd = c1.getInt(c1.getColumnIndex(DBUserProfile.COLUMN.SLEEP_LOG_END));
                mPrimaryProfile.targetDeviceMac = c1.getString(c1.getColumnIndex(DBUserProfile.COLUMN.DEVICE_MAC));

                mPD.saveUserProfile(mPrimaryProfile);

            } else {
            }
            return address;
        } finally {
            DBUtils.closeCursor(c1);
        }
    }

    private void updateUserDeviceData(String macId) {

        Cursor c = getContentResolver().query(CONTENT_URI_DB_DEVICE, new String[]{
                        DBDevice.COLUMN._ID, DBDevice.COLUMN.PROFILE_ID, DBDevice.COLUMN.LAST_DAILY_SYNC,
                        DBDevice.COLUMN.LAST_HOURLY_SYNC,
                        DBDevice.COLUMN.TIMEZONE,
                        DBDevice.COLUMN.NAME,
                        DBDevice.COLUMN.MAC,

                },
                DBDevice.COLUMN.MAC + "=\"" + macId + "\"", null, null);

        int iCount = c.getCount();

        if (iCount == 0) {
            UtilDBG.i("the address " + c + " is not in the device list , macID:: " + macId);

        } else {
            try {
                if (DBUtils.isCursorUsable(c)) {
                    if (iCount > 1) {
                        UtilDBG.e("!! Assume that there is at most one row !!");
                    }
                    c.moveToNext();
                    DeviceData device = new DeviceData();
                    device._Id = c.getLong(c.getColumnIndex(DBDevice.COLUMN._ID));
                    device.profileId = c.getLong(c.getColumnIndex(DBDevice.COLUMN.PROFILE_ID));
                    device.displayName = c.getString(c.getColumnIndex(DBDevice.COLUMN.NAME));
                    device.mac = c.getString(c.getColumnIndex(DBDevice.COLUMN.MAC));
                    device.lastDailySyncTime = c.getLong(c.getColumnIndex(DBDevice.COLUMN.LAST_DAILY_SYNC));
                    device.lastHourlySyncTime = c.getLong(c.getColumnIndex(DBDevice.COLUMN.LAST_HOURLY_SYNC));
                    device.lastTimezoneDiff = c.getInt(c.getColumnIndex(DBDevice.COLUMN.TIMEZONE));
                    mPD.updateUserDeviceData(device);
                }
            }finally {
                DBUtils.closeCursor(c);
            }
        }
    }

    private void checkUserModeType() {
        UserModeType mode = mSharedPref.getUserModeType();
        if (mode == UserModeType.GROUP) {
            Intent intent = new Intent(this, GroupActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkIntentAction() {
        Intent intent = getIntent();
        if (intent.getAction() != null) {
            if (intent.getAction().equals(MXWApp.ACTION_SMART_KEY_FIND_PHONE)) {
                findPhoneNotification();
            } else if (intent.getAction().equals(MXWApp.ACTION_SOS)) {
                sendSOSMessage();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onDestroy() {
        UtilDBG.e("[RYAN] onDestroy !!!!!!!!!!!!!!!!!");
        UtilDBG.logMethod();

        // Maxwell BLE api
        releaseMaxwellBleApi();

        // cancel notification
        NotificationManager notiMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notiMgr.cancel(NOTIFICATION_FIND_PHONE);

//        mPD.closeAll();
        UtilDBG.close();

        unregisterReceiver(mDeviceEventReceiver);
        if (copyAsync != null) {
            copyAsync.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
//        AppEventsLogger.activateApp(this);

        // Do sync ADT data
        mIsOnResumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
//        AppEventsLogger.deactivateApp(this);

        mIsOnResumed = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mActivityResultCallback != null)
            mActivityResultCallback.onResult(requestCode, resultCode, data);
    }

    public boolean isActivityOnResumed() {
        return mIsOnResumed;
    }

    private void checkFirstStart() {

        //setting change
        showSettingsContainer();

    }

    private void showSettingsContainer() {
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (ft == null) {
                UtilDBG.e("showContainerMain, can not get FragmentTransaction");
                return;
            }

            if (mContainerSettings == null) {
                mContainerSettings = new FTabConf();
                ft.add(R.id.container_main, mContainerSettings, CONTAINER_SETTINGS);
            }

            ft.show(mContainerSettings);

            ft.commitAllowingStateLoss();

            getSupportFragmentManager().executePendingTransactions();
        }
    }


    void restoreFragments() {
        if (getSupportFragmentManager() == null) {
            return;
        }

        List<Fragment> listf = getSupportFragmentManager().getFragments();
        if (listf != null) {
            for (Fragment f : listf) {
                if (f == null) {
                    continue;
                }

                if (f.getTag().compareToIgnoreCase(CONTAINER_SETTINGS) == 0) {
                    {
                        if (f instanceof FTabConf) {
                            UtilDBG.i("[[[ mContainerMain restore ]]]");
                            mContainerSettings = (FTabConf) f;

                        }
                    }
                }
            }
        }
    }

    // FIXME : here will crash
    public List<Fragment> getFragmentList(String strTag, boolean bAtMostOne) {
        List<Fragment> fList = new ArrayList<Fragment>();
        if (getSupportFragmentManager() != null) {
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                if (f == null) {
                    continue;
                }

                if (f.getTag().compareToIgnoreCase(strTag) == 0) {
                    fList.add(f);
                }

                if (f.getChildFragmentManager() == null) {
                    continue;
                }

                List<Fragment> childfs = f.getChildFragmentManager().getFragments();
                if (childfs == null) {
                    continue;
                }

                for (Fragment childf : childfs) {
                    if (childf.getTag().compareToIgnoreCase(strTag) == 0) {
                        fList.add(childf);
                    }
                }
            }
        }

        if (bAtMostOne && fList.size() > 1) {
            UtilDBG.e("unexpected MainActivity.getFragmentList bAtMostOne=true, actual:" + fList.size());
        }

        return fList;
    }

    private void stopRingtoneFindPhone() {
        if (mRingtoneFindPhone.isPlaying()) {
            mRingtoneFindPhone.stop();
        }
    }

    private void showNotification(String text) {
        Intent it = new Intent(getApplicationContext(), MainActivity.class);
//        it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add this to bring MainActivity to the top.
        // MainActivity should be set to "singleTop" in AndroidManifest.xml
        it.setAction("android.intent.action.MAIN");

        PendingIntent penIt = PendingIntent.getActivity(getApplicationContext(),
                NOTIFICATION_FIND_PHONE, it, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification noti = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker(text)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setContentIntent(penIt)
                .build();

        NotificationManager notiMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notiMgr.notify(NOTIFICATION_FIND_PHONE, noti);
    }

    private void showFindPhoneAlert() {
        mDlgAlert = new Dialog(this, R.style.dlg_full_screen_bb);
        mDlgAlert.setCancelable(false);
        mDlgAlert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlgAlert.setContentView(R.layout.dlg_find_phone);

        Button btnStopAlert = (Button) mDlgAlert.findViewById(R.id.btnOK);
        btnStopAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStopDlgAlert();
            }
        });

        mDlgAlert.show();
    }

    private void showSOSAlert(String sosMessage) {
        if (mDlgAlert != null) {
            doStopDlgAlert();
        }

        final String titleText = getString(R.string.profile_sos);
        showDlgAlert(titleText, sosMessage);
    }

    private void showOutOfRangeAlert(boolean disconnected) {
        final int titleTextId = R.string.dlg_bt_device_out_of_range_alert_title;
        final int desTextId = (disconnected
                ? R.string.dlg_bt_device_out_of_range_alert_description_far
                : R.string.dlg_bt_device_out_of_range_alert_description_near);

        showDlgAlert(titleTextId, desTextId);
    }

    private void showLowBatteryAlert() {
        final int titleTextId = R.string.dlg_bt_device_low_battery_title;
        final int desTextId = R.string.dlg_low_battery_des;

        showDlgAlert(titleTextId, desTextId);
    }

    private void showDlgAlert(int titleTextId, int desTextId) {
        mDlgAlert = WarningUtil.getDialogOK(
                this,
                titleTextId,
                desTextId,
                R.string.btn_stop_alert,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doStopDlgAlert();
                    }
                });

        mDlgAlert.show();
    }

    private void showDlgAlert(String titleText, String desText) {
        mDlgAlert = WarningUtil.getDialogOK(
                this,
                titleText,
                desText,
                R.string.btn_stop_alert,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doStopDlgAlert();
                    }
                });

        mDlgAlert.show();
    }

    private void doStopDlgAlert() {
        if (mDlgAlert != null) {
            mHandler.removeCallbacks(mRunnableStopAlert);
            stopRingtoneFindPhone();

            mDlgAlert.dismiss();
            mDlgAlert = null;
        }
    }

    private void stopDlgAlertDelay(int ms) {
        mHandler.postDelayed(mRunnableStopAlert, ms);
    }

    private void outOfRangeAlert() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDlgAlert == null) {
                    mRingtoneFindPhone.play();
                    showOutOfRangeAlert(true);
                    stopDlgAlertDelay(DELAY_STOP_ALERT);
                }
            }
        });
    }

    private void findPhoneNotification() {
        UtilDBG.i("MainActivity > findPhoneNotification - mDlgAlert = " + mDlgAlert);
        if (mDlgAlert == null) {
            mRingtoneFindPhone.play();
            showFindPhoneAlert();
            stopDlgAlertDelay(DELAY_STOP_ALERT);

            showNotification(getString(R.string.dlg_bt_device_btn_pressed_find_phone_title));
        } else {
            doStopDlgAlert();
        }
    }

    private void sendSOSMessage() {
        if (mSharedPref.getEmergencyContactPhone().equals("")) {
            return;
        }

        String message = sendMyLocationSMS();
        if (message != null) {
            showSOSAlert(message);
        }
    }

    private void initDevicePowerWatch() {

        // get phone's default language

        mSharedPref.setDeviceLanguage(LanguageType.ENGLISH);
        // User setting
        int stride = (int) mPD.getPersonStride();
        int weight = (int) mPD.getPersonWeight();
        mMaxwellBLE.configUserSetting(stride, weight);

        // Device system setting
        UnitType unitType = mSharedPref.getProfileUnit();
        LanguageType language = mSharedPref.getDeviceLanguage();
        boolean enableVibrate = mSharedPref.isDeviceVibrationEnable();
        mMaxwellBLE.configSystemSetting(unitType, language, enableVibrate);

        // Missed call & sms
        mMaxwellBLE.setMissingCallNumber(MXWApp.getTotalMissedCall(), false);
        mMaxwellBLE.setUnreadMessageNumber(MXWApp.getTotalMissedSMS(), false);

        // Out Of Range
        boolean enableOutOfRange = mSharedPref.isDeviceOutOfRangeEnable();
        mMaxwellBLE.setLinkLossIndicator(enableOutOfRange);

        // Reset fitness filter to default value
        mMaxwellBLE.configFitnessFilter(FitnessType.UNKNOWN);

        // TODO : (TEST) enable SOS
        mMaxwellBLE.enableSOS(mSharedPref.isDeviceSOSEnable());


        // TODO : (TEST) setup time format
//        mMaxwellBLE.configTimeFormat(TimeFormatType.FORMAT_24H);

        // TODO : (TEST) read device information
        mMaxwellBLE.readFWRevision();
        mMaxwellBLE.readHWRevision();
        mMaxwellBLE.readSWRevision();
        mMaxwellBLE.readSerialNumber();

        // TODO : (TEST) read fitness filter
        mMaxwellBLE.readFitnessFilter();
    }

    private String sendMyLocationSMS() {
        mLocationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = mLocationMgr.getBestProvider(new Criteria(), true);
        if (provider == null) {
            UtilDBG.e("no location provider");
            return null;
        }

        Location location = mLocationMgr.getLastKnownLocation(provider);
        if (location == null) {
            UtilDBG.e("cannot not get last location");
            return null;
        }

        UtilDBG.i("[RYAN] onSOSTrigger > Location : " + location.getLatitude() + " | " + location.getLongitude());

        String phoneNumber = mSharedPref.getEmergencyContactPhone(); // "0978995204"
        String contactName = mSharedPref.getEmergencyContactName();
        if (!phoneNumber.equals("")) {

            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";

            // Send SMS message
            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(SENT), 0);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(DELIVERED), 0);

            String link = Html.fromHtml(
                    String.format(getString(R.string.sos_sms_link),
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getLatitude(),
                            location.getLongitude())).toString();

            String myName = mPD.getPersonName();
            String message = String.format(
                    getString(R.string.sos_sms_detail),
                    myName,
                    myName,
                    link,
                    getString(R.string.app_name));

            UtilDBG.i("[RYAN] onSOSTrigger > send SMS to : " + phoneNumber + " | " + myName);
            UtilDBG.i("[RYAN] onSOSTrigger > message : " + message);

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);


            SOSRecord sos = new SOSRecord(
                    UtilTime.getUnixTime(new Date().getTime()),
                    contactName,
                    location.getLatitude(), location.getLongitude()
            );
            mPD.saveSOSRecord(sos);

            return message;
        }

        return null;
    }

    @Override
    public void onDBUpgradeStart() {
        mNeedUpgradDB = true;

        if (mDlgProgress == null) {
            mDlgProgress = new ProgressDialog(this);
            mDlgProgress.setMessage(getString(R.string.progress_upgrade));
            mDlgProgress.show();
        }
    }

    @Override
    public void onDBUpgradeFinish() {
        // TODO : update UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDlgProgress != null) {
                    mDlgProgress.dismiss();
                    mDlgProgress = null;
                }

            }
        });
    }

    private Runnable mRunnableStopAlert = new Runnable() {

        @Override
        public void run() {
            doStopDlgAlert();
        }

    };

    @Override
    public void onDeviceDiscover(MGPeripheral sender, AdvertisingData advertisingData) {
        super.onDeviceDiscover(sender, advertisingData);
    }

    @Override
    public void onDeviceConnect(final MGPeripheral sender) {
        super.onDeviceConnect(sender);
        UtilDBG.e("MainActivity , onDeviceConnect:: " + sender.getTargetAddress());
        UtilDBG.e("MainActivity , onDeviceConnect, salestrack:: " + !mSharedPref.getSalesTrackStatus());
        //send salestrack
        if (!mSharedPref.getSalesTrackStatus()) {
            Intent msgIntent = new Intent(MainActivity.this, SalesTrackService.class);
            msgIntent.putExtra("MacId", sender.getTargetAddress());
            startService(msgIntent);
        }

    }

    @Override
    public void onDeviceDisconnect(MGPeripheral sender) {
        super.onDeviceDisconnect(sender);

        if (mSharedPref.isDeviceOutOfRangeEnable()) {
            outOfRangeAlert();
        }
    }

    @Override
    public void onConnectTimeOut(MGPeripheral sender) {
        super.onConnectTimeOut(sender);
    }

    @Override
    public void onScanDeviceNotFound(MGPeripheral sender) {

    }

    @Override
    public void onRssiUpdate(MGPeripheral sender, int rssi) {

    }

    @Override
    public void onDeviceReady(MGPeripheral sender) {
        // TODO this should happen only once during pairing.
        initDevicePowerWatch();

        if (mPairDeviceListener != null) {
            mPairDeviceListener.onDeviceReady(sender);
        }

        if (mSyncDeviceListener != null) {
            mSyncDeviceListener.onDeviceReady(sender);
        }
    }

    @Override
    public Date getLastDailySyncDate(String deviceAddress) {
        DeviceData device = mPD.getUserDeviceByAddress(deviceAddress);
        if (device != null) {
            long unixTime = device.lastDailySyncTime;
            if (unixTime != 0) {
                Date date = new Date(UtilTime.getMillisecond(unixTime));
                return date;
            }
        }

        return null;
    }

    @Override
    public Date getLastHourlySyncDate(String deviceAddress) {
        DeviceData device = mPD.getUserDeviceByAddress(deviceAddress);
        if (device != null) {
            long unixTime = device.lastHourlySyncTime;
            if (unixTime != 0) {
                Date date = new Date(UtilTime.getMillisecond(unixTime));
                return date;
            }
        }

        return null;
    }

    @Override
    public void updateLastDailySyncDate(String deviceAddress, Date date) {
        UtilDBG.d("[RYAN] MainActivity > updateLastDailySyncDate : " + date);

        DeviceData device = mPD.getUserDeviceByAddress(deviceAddress);
        if (device != null) {
            device.lastDailySyncTime = UtilTime.getUnixTime(date.getTime());
            mPD.updateUserDeviceData(device);
        }
    }

    @Override
    public void updateLastHourlySyncDate(String deviceAddress, Date date) {
//        UtilDBG.d("[RYAN] MainActivity > updateLastHourlySyncDate : " + date);

        DeviceData device = mPD.getUserDeviceByAddress(deviceAddress);
        if (device != null) {
            device.lastHourlySyncTime = UtilTime.getUnixTime(date.getTime());
            mPD.updateUserDeviceData(device);
        }
    }

    @Override
    public void updateDailyEnergyRecord(String deviceAddress, Date date, int energy, int step) {
//        UtilDBG.d("[RYAN] MainActivity > updateDailyEnergyRecord : " + date + " | " + energy + " | " + step);

        mPD.saveDailyRecord(date, energy, step, deviceAddress);
    }

    @Override
    public void updateHourlyEnergyRecord(String deviceAddress, Date date, int energy, int step) {
//        UtilDBG.d("[RYAN] MainActivity > updateHourlyEnergyRecord : " + date + " | " + energy + " | " + step);

        DeviceData device = mPD.getUserDeviceByAddress(deviceAddress);
        if (device != null) {
            int iLastTimezoneDiff = device.lastTimezoneDiff;
            TimeZone tzLastTime = UtilTZ.getTZWithOffset(iLastTimezoneDiff);

            mPD.saveHourlyRecord(date, energy, step, deviceAddress, tzLastTime.getRawOffset() / 1000 / 60);
        }
    }

    @Override
    public void update15MinutesBasedMoveRecord(String deviceAddress, Date date, int move) {
//        UtilDBG.d("[RYAN] MainActivity > updateHourlyMoveRecord : " + date + " | " + move);

        DeviceData device = mPD.getUserDeviceByAddress(deviceAddress);
        if (device != null) {
            int iLastTimezoneDiff = device.lastTimezoneDiff;
            TimeZone tzLastTime = UtilTZ.getTZWithOffset(iLastTimezoneDiff);

            mPD.save15MinBasedSleepMove(date, move, deviceAddress, tzLastTime.getRawOffset() / 1000 / 60);
        }
    }

    @Override
    public void onSmartKeyTrigger() {
        // onSmartKeyTrigger called from MXWApp
    }

    @Override
    public void onSOSTrigger() {
        // onSOSTrigger called from MXWApp
    }

    @Override
    public void onAttributeRead(AttributeType attributeType, AttributeValue attributeValue) {
        UtilDBG.i("[RYAN] onAttributeRead > " + attributeType + " | " + attributeValue);
        switch (attributeType) {
            case CURRENT_HOUR_MOVE:
                if (mFitnessUpdateListener != null) {
                    mFitnessUpdateListener.onMoveUpdate(attributeValue.toInt());
                }
                break;
            case CURRENT_HOUR_STEP:
                if (mFitnessUpdateListener != null) {
                    mFitnessUpdateListener.onStepUpdate(attributeValue.toInt());
                }
                break;
            case BATTERY_LEVEL:
                int level = attributeValue.toInt();
                mSharedPref.setTargetDeviceBattery(level);
                if (level < 20) {
                    if (mDlgAlert == null) {
                        mRingtoneFindPhone.play();
                        showLowBatteryAlert();
                        stopDlgAlertDelay(DELAY_STOP_ALERT);
                        showNotification(getString(R.string.dlg_bt_device_low_battery_title));
                    }
                }
                break;
            case BATTERY_STATUS:

                break;
            case FW_REVISION:
                ((MXWApp) getApplication()).setDevFWRevision(attributeValue.toString());
                break;
            case HW_REVISION:
                ((MXWApp) getApplication()).setDevHWRevision(attributeValue.toString());
                break;
            case SW_REVISION:
                ((MXWApp) getApplication()).setDevSWRevision(attributeValue.toString());
                break;
            case SERIAL_NUMBER:
                ((MXWApp) getApplication()).setDevSerialNumber(attributeValue.toString());
                break;
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        UtilDBG.i("[RYAN] onLocationChanged > Location : " + location.getLatitude() + " | " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class CopyDataAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            copyHMData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String address = mPD.getTargetDeviceMac();
            if (MXWApp.initBleAutoConnection(address) && !mMaxwellBLE.isConnected()) {
                MXWApp.connectDevice(address);
            }
        }
    }

    private BroadcastReceiver mDeviceEventReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            UtilDBG.e("[RYAN] mSmartKeyReceiver > onReceive > action : " + intent.getAction());
            if (intent.getAction().equals(MXWApp.ACTION_SMART_KEY_FIND_PHONE)) {
                findPhoneNotification();
            } else if (intent.getAction().equals(MXWApp.ACTION_SOS)) {
                sendSOSMessage();
            }
        }
    };

}
