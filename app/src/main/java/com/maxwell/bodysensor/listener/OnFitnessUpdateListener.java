package com.maxwell.bodysensor.listener;

import java.util.Date;

/**
 * Created by ryanhsueh on 15/4/24.
 */
public interface OnFitnessUpdateListener {
    void onMoveUpdate(int move);
    void onStepUpdate(int step);
}
