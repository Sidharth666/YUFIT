package com.maxwell.bodysensor.data.group;

import com.maxwell.bodysensor.util.UtilConst;
import com.maxwellguider.bluetooth.activitytracker.GoalType;

/**
 * Created by ryanhsueh on 15/3/17.
 */
public class GroupData {

    public long group_Id = UtilConst.INVALID_INT;
    public String name;
    public String school;
    public String _class;
    public int daily_goal;
    public GoalType goal_type;

}
