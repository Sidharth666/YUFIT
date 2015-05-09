package com.maxwell.bodysensor;

import android.content.Context;
import android.content.SharedPreferences;

import com.maxwell.bodysensor.data.UserModeType;
import com.maxwell.bodysensor.util.UtilConst;
import com.maxwellguider.bluetooth.MGPeripheral.DeviceType;
import com.maxwellguider.bluetooth.activitytracker.GoalType;
import com.maxwellguider.bluetooth.activitytracker.LanguageType;
import com.maxwellguider.bluetooth.activitytracker.UnitType;
import com.maxwell.bodysensor.util.UtilDBG;

// Wrapper for SharedPreferences, wrap related functions
// [1] to make sure the defValue is the same.

public class SharedPrefWrapper {
    private static SharedPrefWrapper mInstance = null;
    private static SharedPreferences mPref = null;
    private static SharedPreferences.Editor mEditor = null;

    private static final int DEF_DEVICE_NO_DISTURBING_START = 1380; // default : 1380 => 23:00
    private static final int DEF_DEVICE_NO_DISTURBING_END = 420; // default : 420 => 7:00

    private static final int DEF_DEVICE_ALARM_TIME = 480; // default : 480 => 8:00
    private static final int DEF_DEVICE_MOVE_ALERT_START = 540; // default : 540 => 9:00
    private static final int DEF_DEVICE_MOVE_ALERT_END = 1080; // default : 1080 => 18:00

    private final String APP_FIRST_START = "app_first_start_db_ver_4";

    private final String KEY_PROFILE_UNIT = "key_profile_unit";

    //========================================
    // BLE device advanced feature
    //========================================

    // Target device info
    private final String KEY_TARGET_DEVICE_BATTERY = "key_target_device_battery";

    // BLE device goal type
    private final String DEVICE_GOAL_TYPE = "device_goal_type";

    // BLE device mute mode
    private final String KEY_PHONE_CONNECTION_ENABLE = "key_phone_connection_enable";

    // BLE device phone notify
    private final String DEVICE_INCOMING_CALL_ENABLE = "device_incoming_call_enable";
    private final String KEY_INCOMING_CALL_NO_DISTURBING_ENABLE = "key_incoming_call_no_disturbing_enable";
    private final String KEY_INCOMING_CALL_NO_DISTURBING_START = "key_incoming_call_no_disturbing_start";
    private final String KEY_INCOMING_CALL_NO_DISTURBING_END = "key_incoming_call_no_disturbing_end";

    // BLE device out of range
    private final String DEVICE_OUT_OF_RANGE_ENABLE = "device_out_of_range_enable";
    private final String KEY_OUT_OF_RANGE_NO_DISTURBING_ENABLE = "key_out_of_range_no_disturbing_enable";
    private final String KEY_OUT_OF_RANGE_NO_DISTURBING_START = "key_out_of_range_no_disturbing_start";
    private final String KEY_OUT_OF_RANGE_NO_DISTURBING_END = "key_out_of_range_no_disturbing_end";

    // BLE device weekly alarm
    private final String DEVICE_WEEKLY_ALARM = "device_weekly_alarm";
    private final String DEVICE_WEEKLY_ALARM_TIME = "device_weekly_alarm_time";

    // BLE device task alert
    private final String DEVICE_TASK_01_ALERT_NAME = "device_task_01_alert_name";
    private final String DEVICE_TASK_01_ALERT_DATE = "device_task_01_alert_date";
    private final String DEVICE_TASK_02_ALERT_NAME = "device_task_02_alert_name";
    private final String DEVICE_TASK_02_ALERT_DATE = "device_task_02_alert_date";
    private final String DEVICE_TASK_03_ALERT_NAME = "device_task_03_alert_name";
    private final String DEVICE_TASK_03_ALERT_DATE = "device_task_03_alert_date";

    // BLE device move alert
    private final String DEVICE_MOVE_ALERT_ENABLE = "device_move_alert_enable";
    private final String DEVICE_MOVE_ALERT_START = "device_move_alert_start";
    private final String DEVICE_MOVE_ALERT_END = "device_move_alert_end";

    // BLE device general setting
    private final String DEVICE_LANGUAGE = "device_language";
    private final String DEVICE_VIBRATION = "device_vibration";
    private final String KEY_FIND_PHONE_ENABLE = "key_find_phone_enable";

    // SOS Emergency Contact
    private final String KEY_SOS_ENABLE = "key_sos_enable";
    private final String KEY_EMERGENCY_CONTACT_NAME = "key_emergency_contact_name";
    private final String KEY_EMERGENCY_CONTACT_EMAIL = "key_emergency_contact_email";
    private final String KEY_EMERGENCY_CONTACT_PHONE = "key_emergency_contact_phone";

    // For Group mode
    private final String CURRENT_MODE_STATS = "current_mode_stats";
    private final String CURRENT_GROUP_ID = "current_group_id";

    private final String APP_COACH_MARK_NEED_SHOW = "app_coach_mark_need_show";
    private final String APP_TAB_TRENDS_NEXT_MODE = "app_tab_trends_next_mode";

    private final String LOCATION_UPDATES_REQUESTED = "location_updates_requested";

    private final String ACCOUNT_EMAIL = "account_email";
    private final String ACCOUNT_PASSWORD = "account_password";

    private final String FB_UID = "fb_uid";
    private final String FB_ACCESS_TOKEN = "fb_access_token";
//    private final String FB_EXPIRES_TIME = "fb_expires_time";
    private final String FB_IS_MAIN_ACCOUNT = "fb_is_main_account";

    private final String WEIBO_UID = "weibo_uid";
    private final String WEIBO_ACCESS_TOKEN = "weibo_access_token";
    private final String WEIBO_EXPIRES_TIME = "weibo_expires_time";
    private final String WEIBO_IS_MAIN_ACCOUNT = "weibo_is_main_account";

    private SharedPrefWrapper() {
        // default constructor hidden, because this is a singleton
    }

    public static void initInstance(Context ctx, String name) {
        if (ctx==null || name==null) {
            UtilDBG.e("SharedPrefWrapper, initInstance, check the parameter");
            return;
        }

        if (mInstance==null) {
            mInstance = new SharedPrefWrapper();
        }

        mPref = ctx.getSharedPreferences(name, Context.MODE_PRIVATE);
        mEditor = mPref.edit();
    }

    public static SharedPrefWrapper getInstance() {
        return mInstance;
    }

    private boolean applyIt(String key, Object o) {
        if (o instanceof Boolean) {
            mEditor.putBoolean(key, ((Boolean) o).booleanValue());
        } else if (o instanceof Integer) {
            mEditor.putInt(key, ((Integer) o).intValue());
        } else if (o instanceof Long) {
            mEditor.putLong(key, ((Long) o).longValue());
        } else if (o instanceof String) {
            mEditor.putString(key, o.toString());
        } else {
            UtilDBG.e("SharedPrefWrapper applyIt, unexpected object type");
            return false;
        }

        mEditor.apply();
        return true;
    }


    public UnitType getProfileUnit() {
        int value = mPref.getInt(KEY_PROFILE_UNIT, UnitType.METRIC.getValue());
        return UnitType.getTypeOfValue(value);
    }
    public boolean setProfileUnit(UnitType unit) {
        return applyIt(KEY_PROFILE_UNIT, unit.getValue());
    }

    //============================================
    // BLE device
    //============================================
    public boolean setTargetDeviceBattery(int battery) {
        return applyIt(KEY_TARGET_DEVICE_BATTERY, battery);
    }
    public int getTargetDeviceBattery() {
        return mPref.getInt(KEY_TARGET_DEVICE_BATTERY, 0);
    }

    public boolean setDeviceGoalType(GoalType type) {
        return applyIt(DEVICE_GOAL_TYPE, type.getValue());
    }
    public GoalType getDeviceGoalType() {
        int value = mPref.getInt(DEVICE_GOAL_TYPE, GoalType.STEP.getValue());
        return GoalType.getTypeOfValue(value);
    }

    //++ BLE device mute
    public boolean isPhoneConnectionEnable() {
        return mPref.getBoolean(KEY_PHONE_CONNECTION_ENABLE, true);
    }
    public boolean enablePhoneConnection(boolean enable) {
        return applyIt(KEY_PHONE_CONNECTION_ENABLE, enable);
    }
    //-- BLE device mute

    //++ BLE device phone notify
    public boolean isDeviceIncomingCallEnable() {
        return mPref.getBoolean(DEVICE_INCOMING_CALL_ENABLE, true);
    }
    public boolean enableDeviceIncomingCall(boolean enable) {
        return applyIt(DEVICE_INCOMING_CALL_ENABLE, enable);
    }
    public boolean isInComingCallNoDisturbingEnable() {
        return mPref.getBoolean(KEY_INCOMING_CALL_NO_DISTURBING_ENABLE, true);
    }
    public boolean enableInComingCallNoDisturbing(boolean enable) {
        return applyIt(KEY_INCOMING_CALL_NO_DISTURBING_ENABLE, enable);
    }
    public int getInComingCallNoDisturbingStart() {
        return mPref.getInt(KEY_INCOMING_CALL_NO_DISTURBING_START, DEF_DEVICE_NO_DISTURBING_START);
    }
    public boolean setInComingCallNoDisturbingStart(int time) {
        return applyIt(KEY_INCOMING_CALL_NO_DISTURBING_START, time);
    }
    public int getInComingCallNoDisturbingEnd() {
        return mPref.getInt(KEY_INCOMING_CALL_NO_DISTURBING_END, DEF_DEVICE_NO_DISTURBING_END);
    }
    public boolean setInComingCallNoDisturbingEnd(int time) {
        return applyIt(KEY_INCOMING_CALL_NO_DISTURBING_END, time);
    }
    //-- BLE device phone notify

    //++ BLE device out of range
    public boolean isDeviceOutOfRangeEnable() {
        return mPref.getBoolean(DEVICE_OUT_OF_RANGE_ENABLE, false);
    }
    public boolean enableDeviceOutOfRange(boolean enable) {
        return applyIt(DEVICE_OUT_OF_RANGE_ENABLE, enable);
    }
    public boolean isOutOfRangeNoDisturbingEnable() {
        return mPref.getBoolean(KEY_OUT_OF_RANGE_NO_DISTURBING_ENABLE, true);
    }
    public boolean enableOutOfRangeNoDisturbing(boolean enable) {
        return applyIt(KEY_OUT_OF_RANGE_NO_DISTURBING_ENABLE, enable);
    }
    public int getOutOfRangeNoDisturbingStart() {
        return mPref.getInt(KEY_OUT_OF_RANGE_NO_DISTURBING_START, DEF_DEVICE_NO_DISTURBING_START);
    }
    public boolean setOutOfRangeNoDisturbingStart(int time) {
        return applyIt(KEY_OUT_OF_RANGE_NO_DISTURBING_START, time);
    }
    public int getOutOfRangeNoDisturbingEnd() {
        return mPref.getInt(KEY_OUT_OF_RANGE_NO_DISTURBING_END, DEF_DEVICE_NO_DISTURBING_END);
    }
    public boolean setOutOfRangeNoDisturbingEnd(int time) {
        return applyIt(KEY_OUT_OF_RANGE_NO_DISTURBING_END, time);
    }
    //-- BLE device out of range

    //++ BLE device weekly alarm
    public int getDeviceWeeklyAlarmMask() {
        return mPref.getInt(DEVICE_WEEKLY_ALARM, 0);
    }
    public boolean setDeviceWeeklyAlarmMask(int weeklyAlarmMask) {
        return applyIt(DEVICE_WEEKLY_ALARM, weeklyAlarmMask);
    }
    public int getDeviceWeeklyAlarmTime() {
        return mPref.getInt(DEVICE_WEEKLY_ALARM_TIME, DEF_DEVICE_ALARM_TIME);
    }
    public boolean setDeviceWeeklyAlarmTime(int time) {
        return applyIt(DEVICE_WEEKLY_ALARM_TIME, time);
    }
    //-- BLE device weekly alarm

    //++ BLE device task alert
    public boolean setDeviceTaskAlert(int index, String name, long datetime) {
    	String keyName = DEVICE_TASK_01_ALERT_NAME;
    	String keyDate = DEVICE_TASK_01_ALERT_DATE;
    	switch(index) {
    	case 1:
    		keyName = DEVICE_TASK_02_ALERT_NAME;
    		keyDate = DEVICE_TASK_02_ALERT_DATE;
    		break;
    	case 2:
    		keyName = DEVICE_TASK_03_ALERT_NAME;
    		keyDate = DEVICE_TASK_03_ALERT_DATE;
    		break;
    	}
    	mEditor.putString(keyName, name);
    	mEditor.putLong(keyDate, datetime);
    	mEditor.apply();

    	return true;
    }
    public String getDeviceTaskAlertName(int index) {
    	String key = DEVICE_TASK_01_ALERT_NAME;
    	switch(index) {
    	case 1:
    		key = DEVICE_TASK_02_ALERT_NAME;
    		break;
    	case 2:
    		key = DEVICE_TASK_03_ALERT_NAME;
    		break;
    	}

    	return mPref.getString(key, "");
    }
    public long getDeviceTaskAlertUnixTime(int index) {
    	String key = DEVICE_TASK_01_ALERT_DATE;
    	switch(index) {
    	case 1:
    		key = DEVICE_TASK_02_ALERT_DATE;
    		break;
    	case 2:
    		key = DEVICE_TASK_03_ALERT_DATE;
    		break;
    	}
    	return mPref.getLong(key, 0);
    }
    //-- BLE device task alert

    //++ BLE device move alert
    public boolean isDeviceMoveAlertEnable() {
        return mPref.getBoolean(DEVICE_MOVE_ALERT_ENABLE, false);
    }
    public boolean enableDeviceMoveAlert(boolean enable) {
        return applyIt(DEVICE_MOVE_ALERT_ENABLE, enable);
    }
    public int getDeviceMoveAlertStart() {
        return mPref.getInt(DEVICE_MOVE_ALERT_START, DEF_DEVICE_MOVE_ALERT_START);
    }
    public boolean setDeviceMoveAlertStart(int time) {
        return applyIt(DEVICE_MOVE_ALERT_START, time);
    }
    public int getDeviceMoveAlertEnd() {
        return mPref.getInt(DEVICE_MOVE_ALERT_END, DEF_DEVICE_MOVE_ALERT_END);
    }
    public boolean setDeviceMoveAlertEnd(int time) {
        return applyIt(DEVICE_MOVE_ALERT_END, time);
    }
    //-- BLE device move alert

    public boolean isDeviceVibrationEnable() {
        return mPref.getBoolean(DEVICE_VIBRATION, true);
    }
    public boolean enableDeviceVibration(boolean vibration) {
        return applyIt(DEVICE_VIBRATION, vibration);
    }

    // 0: English, 1: Simple Chinese, 2: Traditional Chinese
    public LanguageType getDeviceLanguage() {
        int value = mPref.getInt(DEVICE_LANGUAGE, LanguageType.ENGLISH.getValue());
        return LanguageType.getTypeOfValue(value);
    }
    public boolean setDeviceLanguage(LanguageType language) {
        return applyIt(DEVICE_LANGUAGE, language.getValue());
    }

    // 0: none, 1: fine phone, 2: camera, 3: video recorder
    public boolean isFindPhoneEnable() {
        return mPref.getBoolean(KEY_FIND_PHONE_ENABLE, false);
    }

    public boolean enableFindPhone(boolean enable) {
        return applyIt(KEY_FIND_PHONE_ENABLE, enable);
    }

    public boolean enableDeviceSOS(boolean enable) {
        return applyIt(KEY_SOS_ENABLE, enable);
    }
    public boolean isDeviceSOSEnable() {
        return mPref.getBoolean(KEY_SOS_ENABLE, false);
    }

    public boolean saveEmergencyContactName(String name) {
        return applyIt(KEY_EMERGENCY_CONTACT_NAME, name);
    }
    public String getEmergencyContactName() {
        return mPref.getString(KEY_EMERGENCY_CONTACT_NAME, "");
    }

    public boolean saveEmergencyContactEmail(String email) {
        return applyIt(KEY_EMERGENCY_CONTACT_EMAIL, email);
    }
    public String getEmergencyContactEmail() {
        return mPref.getString(KEY_EMERGENCY_CONTACT_EMAIL, "");
    }

    public boolean saveEmergencyContactPhone(String phoneNumber) {
        return applyIt(KEY_EMERGENCY_CONTACT_PHONE, phoneNumber);
    }
    public String getEmergencyContactPhone() {
        return mPref.getString(KEY_EMERGENCY_CONTACT_PHONE, "");
    }


    // APP

    public boolean isAppFirstStart() {
        return mPref.getBoolean(APP_FIRST_START, true);
    }

    public boolean setAppFirstStart(boolean b) {
        return applyIt(APP_FIRST_START, b);
    }

    public boolean isAppCoachMarkNeedShow() {
        return mPref.getBoolean(APP_COACH_MARK_NEED_SHOW, false);
    }

    public boolean setAppCoachMarkNeedShow(boolean b) {
        return applyIt(APP_COACH_MARK_NEED_SHOW, b);
    }

    public int getAppTabTrendsMode() {
        return mPref.getInt(APP_TAB_TRENDS_NEXT_MODE, -1);
    }

    public void setAppTabTrendsMode(int i) {
        applyIt(APP_TAB_TRENDS_NEXT_MODE, i);
    }

    public boolean isContainsLocUpdatesRequested() {
        return mPref.contains(LOCATION_UPDATES_REQUESTED);
    }

    public boolean isLocUpdatesRequested() {
        return mPref.getBoolean(LOCATION_UPDATES_REQUESTED, false);
    }

    public boolean setLocUpdatesRequested(boolean b) {
        return applyIt(LOCATION_UPDATES_REQUESTED, b);
    }


    // Account, email

    public String getAccountEmail() {
        return mPref.getString(ACCOUNT_EMAIL, "");
    }

    public boolean setAccountEmail(String email) {
        return applyIt(ACCOUNT_EMAIL, email);
    }

    // FIXME, encryption
    public String getAccountPassword() {
        return mPref.getString(ACCOUNT_PASSWORD, "");
    }

    public boolean setAccountPassword(String password) {
        return applyIt(ACCOUNT_PASSWORD, password);
    }

    public void clearAccountEmailPref() {
        mEditor.remove(ACCOUNT_EMAIL)
            .remove(ACCOUNT_PASSWORD)
            .apply();
    }

    // FB

    public String getFBUID() {
        return mPref.getString(FB_UID, "");
    }

    public boolean setFBUID(String uid) {
        return applyIt(FB_UID, uid);
    }

    public String getFBAccessToken() {
        return mPref.getString(FB_ACCESS_TOKEN, "");
    }

    public boolean setFBAccessToken(String token) {
        return applyIt(FB_ACCESS_TOKEN, token);
    }

//    public long getFBExpiresTime() {
//        return mPref.getLong(FB_EXPIRES_TIME, 0);
//    }

//    public boolean setFBExpiresTime(long l) {
//        return applyIt(FB_EXPIRES_TIME, l);
//    }

    public boolean isFBMainAccount() {
        return mPref.getBoolean(FB_IS_MAIN_ACCOUNT, false);
    }

    public boolean setFBMainAccount(boolean b) {
        return applyIt(FB_IS_MAIN_ACCOUNT, b);
    }

    public void clearFBPref() {
        mEditor.remove(FB_UID)
            .remove(FB_ACCESS_TOKEN)
//            .remove(FB_EXPIRES_TIME)
            .remove(FB_IS_MAIN_ACCOUNT)
            .apply();
    }

    // WEIBO

    public String getWeiboUID() {
        return mPref.getString(WEIBO_UID, "");
    }

    public boolean setWeiboUID(String uid) {
        return applyIt(WEIBO_UID, uid);
    }

    public String getWeiboAccessToken() {
        return mPref.getString(WEIBO_ACCESS_TOKEN, "");
    }

    public boolean setWeiboAccessToken(String token) {
        return applyIt(WEIBO_ACCESS_TOKEN, token);
    }

    public long getWeiboExpiresTime() {
        return mPref.getLong(WEIBO_EXPIRES_TIME, 0);
    }

    public boolean setWeiboExpiresTime(long l) {
        return applyIt(WEIBO_EXPIRES_TIME, l);
    }

    public boolean isWeiboMainAccount() {
        return mPref.getBoolean(WEIBO_IS_MAIN_ACCOUNT, false);
    }

    public boolean setWeiboMainAccount(boolean b) {
        return applyIt(WEIBO_IS_MAIN_ACCOUNT, b);
    }

    public void clearWeiboPref() {
        mEditor.remove(WEIBO_UID)
            .remove(WEIBO_ACCESS_TOKEN)
            .remove(WEIBO_EXPIRES_TIME)
            .remove(WEIBO_IS_MAIN_ACCOUNT)
            .apply();
    }

    // For Group mode
    public UserModeType getUserModeType() {
        int mode = mPref.getInt(CURRENT_MODE_STATS, UserModeType.USER.getValue());
        return UserModeType.getTypeOfValue(mode);
    }
    public boolean setUserModeType(UserModeType mode) {
        return applyIt(CURRENT_MODE_STATS, mode.getValue());
    }

    public long getShowedGroupId() {
        return mPref.getLong(CURRENT_GROUP_ID, UtilConst.INVALID_INT);
    }
    public boolean setShowedGroupId(long group_id) {
        return applyIt(CURRENT_GROUP_ID, group_id);
    }
}
