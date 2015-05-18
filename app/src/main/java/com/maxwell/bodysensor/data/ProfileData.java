package com.maxwell.bodysensor.data;

import com.maxwell.bodysensor.util.UtilConst;
import com.maxwell.bodysensor.util.UtilDBG;

public class ProfileData {

    public final static double DEFAULT_PERSON_HEIGHT = 170.0;
    public final static double DEFAULT_PERSON_WEIGHT = 60.0;
    public final static double DEFAULT_PERSON_STRIDE = 70.0;
    public final static int DEFAULT_PERSON_SLEEP_BEGIN = 1380; // 23:00
    public final static int DEFAULT_PERSON_SLEEP_END = 420; // 7:00
    public final static int DEFAULT_DAILY_GOAL = 6000;

	public long _Id = UtilConst.INVALID_INT;

	// profile data
	public String name;
	public int gender;
	public long birthday;
	public double height = DEFAULT_PERSON_HEIGHT;
	public double weight = DEFAULT_PERSON_WEIGHT;
	public double stride = DEFAULT_PERSON_STRIDE;
	public byte[] photo;
	public int dailyGoal = DEFAULT_DAILY_GOAL;

    // Sleep
	public int sleepLogBegin = DEFAULT_PERSON_SLEEP_BEGIN;
	public int sleepLogEnd = DEFAULT_PERSON_SLEEP_END;

    // About device
	public String targetDeviceMac = ""; // ADT(all day tracker device)
	public int isPrimaryProfile = UtilConst.TRUE_INT;

    public void updateUserProfile(ProfileData profile) {
    	this._Id = profile._Id;
    	this.name = profile.name;
    	this.gender = profile.gender;
    	this.birthday = profile.birthday;
    	this.height = profile.height;
    	this.weight = profile.weight;
    	this.stride = profile.stride;

    	this.photo = profile.photo;

    	this.dailyGoal = profile.dailyGoal;

    	this.sleepLogBegin = profile.sleepLogBegin;
    	this.sleepLogEnd = profile.sleepLogEnd;

    	this.targetDeviceMac = profile.targetDeviceMac;
    	setIsPrimaryProfile(profile.isPrimaryProfile);
    }

    public void setIsPrimaryProfile(int i) {
    	isPrimaryProfile = i;
        if (isPrimaryProfile!=UtilConst.FALSE_INT && isPrimaryProfile!=UtilConst.TRUE_INT) {
            UtilDBG.e("ProfileData.setIsPrimaryProfile, input=" + Integer.toString(i));
            isPrimaryProfile = UtilConst.FALSE_INT;
        }
    }

}
