/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import java.util.HashMap;

/**
 * Analytics event provider
 */
public interface AnalyticsProvider {

    void onLoggedOut(String deviceId);

    void onLoggedIn(String deviceId, int uid, long phoneNumber, String userName);

    void onLoggedInPerformed(String deviceId, int uid, long phoneNumber, String userName);

    void trackEvent(String event);

    void trackEvent(String event, HashMap<String, String> hashMap);
}