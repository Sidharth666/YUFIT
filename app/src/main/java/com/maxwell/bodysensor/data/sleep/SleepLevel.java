package com.maxwell.bodysensor.data.sleep;

/**
 * Created by ryanhsueh on 15/4/22.
 */
public enum SleepLevel {
    UNKNOWN(0),
    DEEP(1),
    LIGHT(2),
    AWAKE(3);

    private final int value;
    private SleepLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SleepLevel getLevelOfValue(int value) {
        return SleepLevel.values()[value];
    }
}
