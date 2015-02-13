package com.droidkit.engine.event;


public interface NotificationListener {
    void onNotification(int eventType, long eventId, Object[] eventArgs);
}
