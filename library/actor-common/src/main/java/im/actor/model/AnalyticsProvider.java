/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.HashMap;

/**
 * Analytics event provider
 */
public interface AnalyticsProvider {

    /**
     * Called when application is started in logged out state
     *
     * @param deviceId device unique id
     */
    @ObjectiveCName("onLoggedOutWithDeviceId:")
    void onLoggedOut(String deviceId);

    /**
     * Called when application is started in logged in state
     *
     * @param deviceId    device unique id
     * @param uid         uid
     * @param phoneNumber phone number
     * @param userName    user name
     */
    @ObjectiveCName("onLoggedInWithDeviceId:withUid:withPhoneNumber:withUserName:")
    void onLoggedIn(String deviceId, int uid, long phoneNumber, String userName);

    /**
     * Called when user performed log in
     *
     * @param deviceId    device unique id
     * @param uid         uid
     * @param phoneNumber phone number
     * @param userName    user name
     */
    @ObjectiveCName("onLoggedInPerformedWithDeviceId:withUid:withPhoneNumber:withUserName:")
    void onLoggedInPerformed(String deviceId, int uid, long phoneNumber, String userName);

    /**
     * Track Event
     *
     * @param event event name
     */
    @ObjectiveCName("trackEvent:")
    void trackEvent(String event);

    /**
     * Track Event
     *
     * @param event   event name
     * @param hashMap event args
     */
    @ObjectiveCName("trackEvent:withArgs:")
    void trackEvent(String event, HashMap<String, String> hashMap);
}