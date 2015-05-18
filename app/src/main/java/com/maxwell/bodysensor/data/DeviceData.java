package com.maxwell.bodysensor.data;

import com.maxwell.bodysensor.util.UtilConst;

public class DeviceData {
    public long _Id = UtilConst.INVALID_INT;

    public long profileId = UtilConst.INVALID_INT;
    public long lastDailySyncTime;
    public long lastHourlySyncTime;
    public int lastTimezoneDiff;
    public String displayName;
    public String mac;
}
