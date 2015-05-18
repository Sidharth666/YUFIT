package com.maxwell.bodysensor.data.sleep;

/**
 * Created by ryanhsueh on 15/4/22.
 */
public class StateDeep extends State {

    @Override
    public State nextDeepSleep() {
        StateDeep state = new StateDeep();
        state.mScore = this.mScore + 1.0f;
        return state;
    }

    @Override
    public State nextLightSleep() {
        StateLight state = new StateLight();
        state.mScore = this.mScore + 0.8f;
        return state;
    }

    @Override
    public State nextAwake() {
        StateAwake state = new StateAwake();
        state.mScore = this.mScore + 0.7f;
        return state;
    }
}
