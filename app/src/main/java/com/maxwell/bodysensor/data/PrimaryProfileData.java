package com.maxwell.bodysensor.data;

import com.maxwell.bodysensor.util.UtilConst;

public class PrimaryProfileData extends ProfileData {

    private static PrimaryProfileData mProfile;

    private PrimaryProfileData() {
        isPrimaryProfile = UtilConst.TRUE_INT;
    }

    public static synchronized PrimaryProfileData getInstace() {
    	if (mProfile == null) {
    		mProfile = new PrimaryProfileData();
    	}

    	return mProfile;
    }

}
