package com.droidkit.engine.event;

import com.droidkit.engine.event.StateInitValue;

public class State {

    private int eventType;
    private StateInitValue stateInitValue;
    private boolean isStorageEnabled;

    public State(final int eventType, final StateInitValue stateInitValue) {
        this.eventType = eventType;
        this.stateInitValue = stateInitValue;
    }

    public void enableStorage() {
        isStorageEnabled = true;
    }

    public boolean isStorageEnabled() {
        return isStorageEnabled;
    }

    public StateInitValue getStateInitValue() {
        return stateInitValue;
    }
}
