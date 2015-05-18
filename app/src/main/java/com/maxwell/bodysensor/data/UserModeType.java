package com.maxwell.bodysensor.data;

/**
 * Created by ryanhsueh on 15/4/29.
 */
public enum UserModeType {
    USER(0),
    GROUP(1);

    private int value;
    UserModeType(int v) {
        value = v;
    }
    public int getValue() {
        return value;
    }

    public static UserModeType getTypeOfValue(int value) {
        return UserModeType.values()[value];
    }
}
