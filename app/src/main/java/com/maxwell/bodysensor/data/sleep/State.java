package com.maxwell.bodysensor.data.sleep;

/**
 * Created by ryanhsueh on 15/4/22.
 */
public abstract class State {

    protected float mScore;

    public float getSleepScore() {
        return mScore;
    }

    public abstract State nextDeepSleep();
    public abstract State nextLightSleep();
    public abstract State nextAwake();

}
