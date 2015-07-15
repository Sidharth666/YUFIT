package com.maxwell.bodysensor.data;

import com.maxwell.bodysensor.util.UtilConst;

import java.sql.Blob;

public class DeviceData {
    public long _Id = UtilConst.INVALID_INT;

    public long profileId = UtilConst.INVALID_INT;
    public long lastDailySyncTime;
    public long lastHourlySyncTime;
    public int lastTimezoneDiff;
    public String displayName;
    public String mac;
    public int gender;
    public int birthday;
    public int height;
    public int stride;
    public String name;
    public byte[] photo;
    public int dailygoal;
    public int sleeplLogBegin;
    public int sleepLogEnd;
    public String deviceMac;
    public int isPrimaryProfile;


}
