package com.maxwell.bodysensor.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.maxwell.bodysensor.data.group.DBGroup15MinutesRecord;
import com.maxwell.bodysensor.data.group.DBGroupDailyRecord;
import com.maxwell.bodysensor.data.group.DBGroupDevice;
import com.maxwell.bodysensor.data.group.DBGroupHourlyRecord;
import com.maxwell.bodysensor.data.group.DBGroupManager;
import com.maxwell.bodysensor.data.group.DBGroupMemberManager;
import com.maxwell.bodysensor.data.group.DBGroupSleepLog;
import com.maxwell.bodysensor.data.group.DBGroupSleepScore;
import com.maxwell.bodysensor.data.group.GroupData;
import com.maxwell.bodysensor.data.group.GroupMemberData;
import com.maxwell.bodysensor.data.sos.DBSOSRecordManager;
import com.maxwell.bodysensor.data.sos.SOSRecord;
import com.maxwell.bodysensor.data.user.DBUser15MinutesRecord;
import com.maxwell.bodysensor.data.user.DBUserDailyRecord;
import com.maxwell.bodysensor.data.user.DBUserDevice;
import com.maxwell.bodysensor.data.user.DBUserHourlyRecord;
import com.maxwell.bodysensor.data.user.DBUserProfile;
import com.maxwell.bodysensor.data.user.DBUserSleepLog;
import com.maxwell.bodysensor.data.user.DBUserSleepScore;
import com.maxwell.bodysensor.util.UtilCalendar;

import java.util.Date;
import java.util.List;

public class DBProgramData extends SQLiteOpenHelper {

    private static DBProgramData instance = null;
    private SQLiteDatabase mDB;

    private DBUserProfile           mUserProfile = null;

    private DBUserDevice            mUserDevice = null;
    private DBGroupDevice           mGroupDevice = null;

    private DBUserHourlyRecord      mUserRecordManager = null;
    private DBGroupHourlyRecord     mGroupHourlyRecord = null;

    private DBUserDailyRecord       mUserDailyRecord = null;
    private DBGroupDailyRecord      mGroupDailyRecord = null;

    private DBUserSleepScore        mUserSleepScore = null;
    private DBGroupSleepScore       mGroupSleepScore = null;

    private DBUser15MinutesRecord   mUser15MinutesRecord = null;
    private DBGroup15MinutesRecord  mGroup15MinutesRecord = null;

    private DBUserSleepLog          mUserSleepLog = null;
    private DBGroupSleepLog         mGroupSleepLog = null;


    private DBSOSRecordManager      mSOSRecordManager = null;

    // Singleton related functions
    private DBProgramData(Context context,
                          String name, CursorFactory factory,
            int version) {
        super(context, name, factory, version);
    }

    public static void initInstance(Context ctx) {
        if (instance==null) {
            instance = new DBProgramData(ctx, DBUtils.DB_NAME, null, DBUtils.VERSION);
        }
    }

    public static DBProgramData getInstance() {
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Called when the database is created for the "first" time

    	DBUtils.createUserDB(db);
        DBUtils.createGroupDB(db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        // Called when the database needs to be upgraded.

//        if (newVersion > oldVersion) {
//            String packageName = mActivity.getApplicationContext().getPackageName();
//            UtilDBG.i("DBProgramData > package name : " + packageName);
//
//            if (packageName.equals(UtilPackage.PACKAGE_NAME_MAXWELL)) {
//                DBUpgradeWrapper.initInstance();
//                DBUpgradeWrapper DBUpgrade = DBUpgradeWrapper.getInstance();
//                DBUpgrade.setDBUpgradeListener(mActivity);
//                DBUpgrade.upgradeDB(db, oldVersion, newVersion);
//            }
//        }

        DBUtils.deleteDBVer3(db);
        DBUtils.createUserDB(db);
        DBUtils.createGroupDB(db);
    }

    public void initAll() {
        mDB = getWritableDatabase();

        // Initial DBProfileManager
        DBUserProfile.init(mDB);
        mUserProfile = DBUserProfile.getInstance();


        //+++ Device
        DBUserDevice.init(mDB, this);
        mUserDevice = DBUserDevice.getInstance();


        DBGroupDevice.init(mDB, this);
        mGroupDevice = DBGroupDevice.getInstance();
        //--- Device


        //+++ Hourly Record
        DBUserHourlyRecord.init(mDB, this);
        mUserRecordManager = DBUserHourlyRecord.getInstance();

        DBGroupHourlyRecord.init(mDB, this);
        mGroupHourlyRecord = DBGroupHourlyRecord.getInstance();
        //--- Hourly Record


        //+++ Daily Record
        DBUserDailyRecord.init(mDB, this);
        mUserDailyRecord = DBUserDailyRecord.getInstance();

        DBGroupDailyRecord.init(mDB, this);
        mGroupDailyRecord = DBGroupDailyRecord.getInstance();
        //--- Daily Record


        //+++ Sleep Score
        DBUserSleepScore.init(mDB, this);
        mUserSleepScore = DBUserSleepScore.getInstance();

        DBGroupSleepScore.init(mDB, this);
        mGroupSleepScore = DBGroupSleepScore.getInstance();
        //--- Sleep Score


        //+++ Sleep Move
        DBUser15MinutesRecord.init(mDB, this);
        mUser15MinutesRecord = DBUser15MinutesRecord.getInstance();

        DBGroup15MinutesRecord.init(mDB, this);
        mGroup15MinutesRecord = DBGroup15MinutesRecord.getInstance();
        //--- Sleep Move


        //+++ Sleep Log
        DBUserSleepLog.init(mDB, this);
        mUserSleepLog = DBUserSleepLog.getInstance();

        DBGroupSleepLog.init(mDB, this);
        mGroupSleepLog = DBGroupSleepLog.getInstance();
        //--- Sleep Log


        // initial DBSOSRecordManager
        DBSOSRecordManager.init(mDB);
        mSOSRecordManager = DBSOSRecordManager.getInstance();

        initPrimaryProfile();
    }

    public void closeAll() {
        mUserProfile.releaseInstance();
        mUserDevice.releaseInstance();
        mUserRecordManager.releaseInstance();
        mUserDailyRecord.releaseInstance();
        mUserSleepScore.releaseInstance();
        mUser15MinutesRecord.releaseInstance();
        mUserSleepLog.releaseInstance();

        mDB.close();
        mDB = null;
    }

// User Profile ++++++++++++++++++++++++++++++++++++++
    private void initPrimaryProfile() {
    	mUserProfile.initPrimaryProfile();
    }

    public void saveUserProfile(ProfileData profile) {
        Log.e("DBPD", "save up");
    	PrimaryProfileData user = mUserProfile.getPrimaryProfile();
        user.name = profile.name;
        user.gender = profile.gender;
        user.birthday = profile.birthday;
        user.height = profile.height;
        user.weight = profile.weight;
        user.stride = profile.stride;
        user.photo = profile.photo;
        user.sleepLogBegin = profile.sleepLogBegin;
        user.sleepLogEnd = profile.sleepLogEnd;

    	mUserProfile.saveUserProfile();
    }

    // profile remote id
    public long getProfileId() {
        return mUserProfile.getPersonId();
    }

    // photo raw data
    public byte[] getPersonPhotoData() {
    	return mUserProfile.getPersonPhotoData();
    }

    // username
    public String getPersonName() {
    	return mUserProfile.getPersonName();
    }

    // birthday
    public long getPersonBirthday() {
    	return mUserProfile.getPersonBirthday();
    }

    // gender, 0: male, 1: female
    public int getPersonGender() {
        return mUserProfile.getPersonGender();
    }

    // height, cm
    public double getPersonHeight() {
    	return mUserProfile.getPersonHeight();
    }

    // height, kg
    public double getPersonWeight() {
        return mUserProfile.getPersonWeight();
    }

    // stride, cm
    public double getPersonStride() {
        return mUserProfile.getPersonStride();
    }

    // goal
    public int getPersonGoal() {
        return mUserProfile.getPersonGoal();
    }
    public void setPersonGoal(int goal) {
    	mUserProfile.setPersonGoal(goal);
    }

    // Focused Energy Capsule, use device address as ID
    public String getTargetDeviceMac() {
        return mUserProfile.getTargetDeviceMac();
    }
    public void setTargetDeviceMac(String address) {
    	mUserProfile.setTargetDeviceMac(address);
    }

    public int getPersonSMBegin() {
    	return mUserProfile.getPersonSMBegin();
    }
    public int getPersonSMEnd() {
        return mUserProfile.getPersonSMEnd();
    }


// User Profile --------------------------------------------


// Device ++++++++++++++++++++++++++++++++++++++
    public List<DeviceData> getDeviceList() {
        return mUserDevice.getDeviceList();
    }

    public void updateUserDeviceData(DeviceData device) {
        mUserDevice.updateDeviceData(device);
    }

    public DeviceData getUserDeviceByAddress(String strAddress) {
        return mUserDevice.getDeviceDataByAddress(strAddress);
    }

    public void removeUserDevice(String strAddress) {
        mUserDevice.removeDevice(strAddress);
    }
// Device --------------------------------------------

// ADT record ++++++++++++++++++++++++++++++++++++++
    public void saveDailyRecord(Date date, int energy, int step, String deviceMac) {
        mUserDailyRecord.saveDailyRecord(date, energy, step, deviceMac);
    }

    public void saveHourlyRecord(Date date, int energy, int step, String deviceMac, int iMinuteOffset) {
        mUserRecordManager.saveHourlyRecord(date, energy, step, deviceMac, iMinuteOffset);
    }

    public void save15MinBasedSleepMove(Date date, int move, String deviceMac, int iMinuteOffset) {
        mUser15MinutesRecord.save15MinBasedSleepMove(date, move, deviceMac, iMinuteOffset);
    }

    public int addSleepLog(UtilCalendar date, UtilCalendar start, UtilCalendar stop, String deviceMac) {
        return mUserSleepLog.addSleepLog(date, start, stop, deviceMac);
    }

    public List<DailyRecordData> queryDailyData(UtilCalendar begin, UtilCalendar end, UtilCalendar today, String deviceMac) {
        return mUserDailyRecord.queryDailyData(begin, end, today, deviceMac);
    }

    public List<HourlyRecordData> queryHourlyData(UtilCalendar begin, UtilCalendar end, String deviceMac) {
        return mUserRecordManager.queryHourlyData(begin, end, deviceMac);
    }

    public SleepScore calculateSleepScore(UtilCalendar date, UtilCalendar startTime, UtilCalendar stopTime, String deviceMac) {
        return mUserSleepScore.calculateSleepScore(date, startTime, stopTime, deviceMac);
    }

    public int querySleepLogSize(UtilCalendar date, String deviceMac) {
        return mUserSleepLog.querySleepLogSize(date, deviceMac);
    }

    public SleepLogData querySleepLog(UtilCalendar date, int iIndex, String deviceMac) {
        return mUserSleepLog.querySleepLog(date, iIndex, deviceMac);
    }

    public void deleteSleepLog(UtilCalendar date, int iIndex, String deviceMac) {
        mUserSleepLog.deleteSleepLog(date, iIndex, deviceMac);
    }

    public List<DSleepData> queryDSleepData(UtilCalendar begin, UtilCalendar end, String deviceMac) {
        return mUserSleepScore.queryDSleepData(begin, end, deviceMac);
    }
// ADT record --------------------------------------------

// SOS record ++++++++++++++++++++++++++++++++++++++++++++
    public void saveSOSRecord(SOSRecord sos) {
        mSOSRecordManager.saveSOSRecord(sos);
    }

    public List<SOSRecord> getAllSOSRecords() {
        return mSOSRecordManager.getAllSOSRecords();
    }
// SOS record --------------------------------------------

// For Group Mode ++++++++++++++++++++
    public long addNewGroup(GroupData group) {
        return DBGroupManager.addNewGroup(mDB, group);
    }

    public long addNewMember(GroupMemberData member) {
        return DBGroupMemberManager.addNewMember(mDB, member);
    }

    public long updateGroup(GroupData group) {
        return DBGroupManager.updateGroup(mDB, group);
    }

    public long updateMemberProfile(GroupMemberData member) {
        return DBGroupMemberManager.updateMemberProfile(mDB, member);
    }

    public List<GroupData> getListAllGroup() {
        return DBGroupManager.getListAllGroup(mDB);
    }

    public GroupData getGroupData(long group_id) {
        return DBGroupManager.getGroupData(mDB, group_id);
    }

    public List<GroupMemberData> getListGroupMember(long group_id) {
        return DBGroupMemberManager.getListGroupMember(mDB, group_id);
    }

    public GroupMemberData getGroupMember(long member_id) {
        return DBGroupMemberManager.getGroupMember(mDB, member_id);
    }

    public DeviceData getGroupDeviceByAddress(String strAddress) {
        return mGroupDevice.getDeviceDataByAddress(strAddress);
    }

    public void updateGroupDeviceData(DeviceData device) {
        mGroupDevice.updateDeviceData(device);
    }

    public int setMemberTargetDeviceMac(long member_id, String macAddress) {
        return DBGroupMemberManager.updateGroupMemberDeviceMac(mDB, member_id, macAddress);
    }

    public void saveGroupDailyRecord(Date date, int energy, int step, String deviceMac, long group_id, long member_id) {
        mGroupDailyRecord.saveDailyRecord(date, energy, step, deviceMac, group_id, member_id);
    }

    public void saveGroupHourlyRecord(Date date, int energy, int step, String deviceMac, int iMinuteOffset, long member_id) {
        mGroupHourlyRecord.saveHourlyRecord(date, energy, step, deviceMac, iMinuteOffset, member_id);
    }

    public void saveGroup15MinBasedSleepMove(Date date, int move, String deviceMac, int iMinuteOffset, long member_id) {
        mGroup15MinutesRecord.save15MinBasedSleepMove(date, move, deviceMac, iMinuteOffset, member_id);
    }

    public List<HourlyRecordData> queryGroupHourlyData(UtilCalendar begin, UtilCalendar end, String deviceMac) {
        return mGroupHourlyRecord.queryHourlyData(begin, end, deviceMac);
    }

    public List<DailyRecordData> queryGroupDailyData(UtilCalendar begin, UtilCalendar end, UtilCalendar today,
                                                String deviceMac, int daily_goal) {
        return mGroupDailyRecord.queryDailyData(begin, end, today, deviceMac, daily_goal);
    }

    public int queryGroupSleepLogSize(UtilCalendar date, String deviceMac) {
        return mGroupSleepLog.querySleepLogSize(date, deviceMac);
    }

    public SleepLogData queryGroupSleepLog(UtilCalendar date, int iIndex, String deviceMac) {
        return mUserSleepLog.querySleepLog(date, iIndex, deviceMac);
    }

    public SleepScore calculateGroupSleepScore(UtilCalendar date, UtilCalendar startTime, UtilCalendar stopTime, String deviceMac) {
        return mGroupSleepScore.calculateSleepScore(date, startTime, stopTime, deviceMac);
    }

    public List<DSleepData> queryGroupDSleepData(UtilCalendar begin, UtilCalendar end, String deviceMac) {
        return mGroupSleepScore.queryDSleepData(begin, end, deviceMac);
    }

//    public boolean removeGroupDevice(long member_id, String macAddress) {
//        int num = mDeviceManager.removeGroupDevice(member_id, macAddress);
//        boolean bReturn = num > 0;
//        if (bReturn) {
//            updateGroupMemberDeviceMac(member_id, "");
//        }
//        return bReturn;
//    }
//
//    public boolean deleteGroup(long group_id) {
//        List<GroupMemberData> memberList = getListGroupMember(group_id);
//
//        boolean bReturn = DBGroupManager.deleteGroup(mDB, group_id);
//
//        if (bReturn) {
//            for (GroupMemberData member : memberList) {
//                deleteGroupMember(member.member_Id, member.targetDeviceMac);
//            }
//        }
//
//        return bReturn;
//    }
//
//    public boolean deleteGroupMember(long member_id, String macAddress) {
//        // delete member profile
//        int num = DBGroupMemberManager.deleteGroupMember(mDB, member_id);
//
//        // delete member device
//        if (!macAddress.equals("")) {
//            removeGroupDevice(member_id, macAddress);
//        }
//
//        mHourRecordManager.deleteGroupRecord(member_id);
//        mDayRecordManager.deleteGroupRecord(member_id);
//        mSleepRecordManager.deleteGroupRecord(member_id);
//        mSleepLogManager.deleteGroupRecord(member_id);
//        mSleepScoreManager.deleteGroupRecord(member_id);
//
//        return num > 0;
//    }
// For Group Mode --------------------

}
