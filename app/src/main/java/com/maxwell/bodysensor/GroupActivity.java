package com.maxwell.bodysensor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import com.maxwell.bodysensor.data.DBProgramData;
import com.maxwell.bodysensor.data.DeviceData;
import com.maxwell.bodysensor.data.group.GroupData;
import com.maxwell.bodysensor.data.group.GroupMemberData;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgSwitchMode;
import com.maxwell.bodysensor.dialogfragment.group.DFAddNewGroup;
import com.maxwell.bodysensor.fragment.group.FContainerGroup;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilTZ;
import com.maxwell.bodysensor.util.UtilTime;
import com.maxwellguider.bluetooth.AdvertisingData;
import com.maxwellguider.bluetooth.MGPeripheral;
import com.maxwellguider.bluetooth.command.AttributeValue;
import com.maxwellguider.bluetooth.command.feature.AttributeType;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by ryanhsueh on 15/4/28.
 */
public class GroupActivity extends MXWActivity {

    public static final String TAB_SPEC_GROUP_STATS = "GROUP_STATS";
    public static final String TAB_SPEC_GROUPS = "GROUPS";

    public static final String CONTAINER_GROUP = "ContainerGroup";

    private FContainerGroup mContainerGroup;

    private GroupMemberData mMemberOfSyncing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.maxwell.bodysensor.R.layout.activity_main);

        mSharedPref = SharedPrefWrapper.getInstance();

        mPD = DBProgramData.getInstance();

        initMaxwellBleApi();
        MXWApp.disableAutoConnection();

        showContainerGroup();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        List<GroupData> listGroup = mPD.getListAllGroup();
        if (listGroup.size() == 0) {
            DFAddNewGroup dlg = new DFAddNewGroup();
            dlg.showHelper(this);
        }
    }

    public void showContainerGroup() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (ft==null) {
            UtilDBG.e("showContainerGroup, can not get FragmentTransaction");
            return;
        }

        if (mContainerGroup==null) {
            mContainerGroup = new FContainerGroup();
            ft.add(com.maxwell.bodysensor.R.id.container_main, mContainerGroup, CONTAINER_GROUP);
        }

        ft.show(mContainerGroup);

        ft.commitAllowingStateLoss();

        getSupportFragmentManager().executePendingTransactions();
    }

    public void showSwitchModeDialog() {
        DlgSwitchMode dlg = new DlgSwitchMode();
        dlg.setSwitchModeButton(
                getString(com.maxwell.bodysensor.R.string.switch_user_mode),
                new DlgSwitchMode.btnHandler() {
                    @Override
                    public boolean onBtnHandler() {
//                        mSharedPref.setUserModeType(UserModeType.USER);

                        Intent intent = new Intent(GroupActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    }
                }
        ).showHelper(GroupActivity.this);
    }

    public void updateGroupMemberForSync(GroupMemberData member) {
        mMemberOfSyncing = member;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releaseMaxwellBleApi();
    }

    @Override
    public void onAttributeRead(AttributeType attributeType, AttributeValue attributeValue) {

    }

    @Override
    public void onDeviceDiscover(MGPeripheral sender, AdvertisingData advertisingData) {
        super.onDeviceDiscover(sender, advertisingData);
    }

    @Override
    public void onDeviceConnect(final MGPeripheral sender) {
        super.onDeviceConnect(sender);
    }

    @Override
    public void onDeviceDisconnect(MGPeripheral sender) {
        super.onDeviceDisconnect(sender);
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
        if (mPairDeviceListener != null) {
            mPairDeviceListener.onDeviceReady(sender);
        }

        if (mSyncDeviceListener != null) {
            mSyncDeviceListener.onDeviceReady(sender);
        }
    }

    @Override
    public Date getLastDailySyncDate(String deviceAddress) {
        DeviceData device = mPD.getGroupDeviceByAddress(deviceAddress);
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
        DeviceData device = mPD.getGroupDeviceByAddress(deviceAddress);
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
//        UtilDBG.d("[RYAN] GroupActivity > updateLastDailySyncDate : " + date);

        DeviceData device = mPD.getGroupDeviceByAddress(deviceAddress);
        if (device != null) {
            device.lastDailySyncTime = UtilTime.getUnixTime(date.getTime());
            mPD.updateGroupDeviceData(device);
        }
    }

    @Override
    public void updateLastHourlySyncDate(String deviceAddress, Date date) {
//        UtilDBG.d("[RYAN] GroupActivity > updateLastHourlySyncDate : " + date);

        DeviceData device = mPD.getGroupDeviceByAddress(deviceAddress);
        if (device != null) {
            device.lastHourlySyncTime = UtilTime.getUnixTime(date.getTime());
            mPD.updateGroupDeviceData(device);
        }
    }

    @Override
    public void updateDailyEnergyRecord(String deviceAddress, Date date, int energy, int step) {
        mPD.saveGroupDailyRecord(date, energy, step, deviceAddress,
                mMemberOfSyncing.group_Id, mMemberOfSyncing.member_Id);
    }

    @Override
    public void updateHourlyEnergyRecord(String deviceAddress, Date date, int energy, int step) {
        DeviceData device = mPD.getGroupDeviceByAddress(deviceAddress);
        if (device != null) {
            int iLastTimezoneDiff = device.lastTimezoneDiff;
            TimeZone tzLastTime = UtilTZ.getTZWithOffset(iLastTimezoneDiff);
            int iMinuteOffset = tzLastTime.getRawOffset() / 1000 / 60;

            mPD.saveGroupHourlyRecord(date, energy, step,
                    deviceAddress, iMinuteOffset, mMemberOfSyncing.member_Id);
        }
    }

    @Override
    public void update15MinutesBasedMoveRecord(String deviceAddress, Date date, int move) {
        DeviceData device = mPD.getGroupDeviceByAddress(deviceAddress);
        if (device != null) {
            int iLastTimezoneDiff = device.lastTimezoneDiff;
            TimeZone tzLastTime = UtilTZ.getTZWithOffset(iLastTimezoneDiff);
            int iMinuteOffset = tzLastTime.getRawOffset() / 1000 / 60;

            mPD.saveGroup15MinBasedSleepMove(date, move, deviceAddress, iMinuteOffset, mMemberOfSyncing.member_Id);
        }
    }

    @Override
    public void onSmartKeyTrigger() {
        // No smart key functions in group mode
    }

    @Override
    public void onSOSTrigger() {
        // No SOS functions in group mode
    }

}
