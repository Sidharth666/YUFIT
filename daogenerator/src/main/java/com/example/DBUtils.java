package com.example;

public class DBUtils {
	public static final String DB_NAME = "bs.db";

	public static final String COL_ID = "_id";

    public class PROFILE {
        public static final String TABLE = "Profile";

        public static final String COL_GENDER = "gender";
        public static final String COL_BIRTHDAY = "birthdate";
        public static final String COL_HEIGHT = "height";
        public static final String COL_WEIGHT = "weight";
        public static final String COL_STRIDE = "stride";
        public static final String COL_NAME = "name";
        public static final String COL_PHOTO = "photo";
        public static final String COL_DAILY_GOAL = "dailyGoal";
        public static final String COL_SLEEP_LOG_BEGIN = "sleepLogBegin";
        public static final String COL_SLEEP_LOG_END = "sleepLogEnd";
        public static final String COL_TARGET_ADT_MAC = "targetDeviceMac";
        public static final String COL_IS_PRIMARY_PROFILE = "isPrimaryProfile";
    }
    
    // DBDevice
    public class DEVICE {
        public static final String TABLE = "Device";

        public static final String COL_PROFILE_ID = "profileId";
        public static final String COL_LAST_DAILY_SYNC = "lastDailySyncDate";
        public static final String COL_LAST_HOURLY_SYNC = "lastHourlySyncDate";
        public static final String COL_TIMEZONE = "timezone";
        public static final String COL_NAME = "name";              // the name, defined by the user.
        public static final String COL_MAC = "mac";
        public static final String COL_TYPE = "devType"; // P07, e2max, ... (ADT)
        public static final String COL_BATTERY = "battery";
    }

// Trend/Sleep Record +++++++++++++++++++++++++++++++++++++++

    // DBDailyRecord
    public class DailyRecord {
        public static final String TABLE = "DailyRecord";
        
        public static final String COL_PROFILE_ID = "profileId";
        public static final String COL_DATE = "date";
        public static final String COL_GOAL = "dailyGoal";
        public static final String COL_PEDO = "pedo";
        public static final String COL_APP_ENERGY = "energy"; // AppEnergy
        public static final String COL_DISTANCE = "distance"; // kilometers
        public static final String COL_CALORIES = "calories";
    }
    

    // DBHourlyRecord
    public class HourlyRecord {
        public static final String TABLE = "HourlyRecord";

        public static final String COL_PROFILE_ID = "profileId";
        public static final String COL_DATE = "date";
        public static final String COL_PEDO = "pedo";
        public static final String COL_APP_ENERGY = "energy"; // AppEnergy
        public static final String COL_DISTANCE = "distance"; // kilometers
        public static final String COL_CALORIES = "calories";
    }
    

    // DBSleepRecord
    public class MoveRecord {
        public static final String TABLE = "MoveRecord";
        
        public static final String COL_PROFILE_ID = "profileId";
        public static final String COL_DATE = "date";
        public static final String COL_MOVE = "moveCount";
    }

    // DBAlarmRecord
    public class SleepLog {
        public static final String TABLE = "SleepLog";
        
        public static final String COL_PROFILE_ID = "profileId";
        public static final String COL_DATE = "date";        // the data is belonged to which day
        public static final String COL_START_TIME = "startData";   // the actual start point
        public static final String COL_STOP_TIME = "stopData";     // the actual stop point
    }

    // DBDailySleepScoreRecord
    public class DailySleepScore {
        public static final String TABLE = "DailySleepScore";

        public static final String COL_PROFILE_ID = "profileId";
        public static final String COL_DATE = "date";
        public static final String COL_SCORE = "sleepScore";
        public static final String COL_DURATION = "duration";
        public static final String COL_TIMESWOKE = "timesWoke";
    }
    
 // Trend/Sleep Record ---------------------------------------


    // Profile Porjection
//    public static final String[] PROFILE_PROJECTION = {
//    		COL_ID,
//    		Profile.COL_NAME,
//    		Profile.COL_GENDER,
//    		Profile.COL_BIRTHDAY,
//    		Profile.COL_HEIGHT,
//    		Profile.COL_WEIGHT,
//    		Profile.COL_STRIDE,
//    		Profile.COL_PHOTO,
//    	    Profile.COL_CURRENT_ENERGY_GOAL,
//    	    Profile.COL_SLEEP_MONITOR_ON,
//    	    Profile.COL_SLEEP_MONITOR_BEGIN,
//    	    Profile.COL_SLEEP_MONITOR_END,
//    	    Profile.COL_ALARM_ON,
//    	    Profile.COL_ALARM_TIME,
//    	    Profile.COL_ALARM_SNOOZE,
//    	    Profile.COL_ALARM_SNOOZE_INTERVAL,
//    	    Profile.COL_CURRENT_EC_MAC,
//    	    Profile.COL_CURRENT_BC_MAC,
//    	    Profile.COL_IS_PRIMARY_PROFILE,
//    };

}
