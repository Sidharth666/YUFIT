package com.maxwell.bodysensor.data.sos;

import java.util.Date;

/**
 * Created by ryanhsueh on 15/4/24.
 */
public class SOSRecord {

    public long dateUnixTime;
    public String contactName;
    public double latitude;
    public double longitude;

    public SOSRecord(long dateUnixTime, String contactName, double latitude, double longitude) {
        this.dateUnixTime = dateUnixTime;
        this.contactName = contactName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
