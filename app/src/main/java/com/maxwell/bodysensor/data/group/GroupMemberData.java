package com.maxwell.bodysensor.data.group;

import com.maxwell.bodysensor.util.UtilConst;

/**
 * Created by ryanhsueh on 15/3/17.
 */
public class GroupMemberData {

    public final static double PERSON_HEIGHT_DEFAULT = 120.0;
    public final static double PERSON_WEIGHT_DEFAULT = 30.0;
    public final static double PERSON_STRIDE_DEFAULT = 40.0;
    public final static int PERSON_SLEEP_BEGIN_DEFAULT = 1380; // 23:00
    public final static int PERSON_SLEEP_END_DEFAULT = 420; // 7:00

    public long member_Id = UtilConst.INVALID_INT;

    // profile data
    public String name;
    public int gender;
    public long birthday;
    public double height = PERSON_HEIGHT_DEFAULT;
    public double weight = PERSON_WEIGHT_DEFAULT;
    public double stride = PERSON_STRIDE_DEFAULT;
    public byte[] photo = null;

    // Sleep
    public int sleepLogBegin = PERSON_SLEEP_BEGIN_DEFAULT;
    public int sleepLogEnd = PERSON_SLEEP_END_DEFAULT;

    // About device
    public String targetDeviceMac = ""; // ADT(all day tracker device)

    public long group_Id;
    public String school_Id;
    public int seatNumber;
    public String email;

    public boolean isSynced = false;

}
