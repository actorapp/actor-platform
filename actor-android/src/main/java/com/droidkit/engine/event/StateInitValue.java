package com.droidkit.engine.event;

public interface StateInitValue {
    Object[] initState(int eventType, long eventId);
}
