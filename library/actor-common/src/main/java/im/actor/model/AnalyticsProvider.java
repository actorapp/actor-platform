package im.actor.model;

import java.util.HashMap;

/**
 * Created by ex3ndr on 30.04.15.
 */
public interface AnalyticsProvider {

    void onLoggedOut(String deviceId);

    void onLoggedIn(String deviceId, int uid, long phoneNumber, String userName);

    void onLoggedInPerformed(String deviceId, int uid, long phoneNumber, String userName);

    void trackEvent(String event);

    void trackEvent(String event, HashMap<String, String> hashMap);
}