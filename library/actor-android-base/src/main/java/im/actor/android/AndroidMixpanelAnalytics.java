/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.android;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import im.actor.model.AnalyticsProvider;

public class AndroidMixpanelAnalytics implements AnalyticsProvider {

    public static MixpanelAPI getRegisteredApi() {
        return mixpanel;
    }

    private static MixpanelAPI mixpanel;

    public AndroidMixpanelAnalytics(Context context, String token) {

        mixpanel = MixpanelAPI.getInstance(context, token);

        try {
            JSONObject deviceProperties = new JSONObject();
            deviceProperties.put("app", "android");
            mixpanel.registerSuperProperties(deviceProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveDeviceId(String deviceId) {
        try {
            JSONObject deviceProperties = new JSONObject();
            deviceProperties.put("deviceId", deviceId);
            mixpanel.registerSuperProperties(deviceProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoggedOut(String deviceId) {
        saveDeviceId(deviceId);
        mixpanel.identify("device:" + deviceId);
        mixpanel.getPeople().identify("device:" + deviceId);
    }

    @Override
    public void onLoggedIn(String deviceId, int uid, long phoneNumber, String userName) {
        saveDeviceId(deviceId);
        mixpanel.identify("uid:" + uid);
        mixpanel.getPeople().identify("uid:" + uid);
        mixpanel.getPeople().set("$phone", phoneNumber);
        mixpanel.getPeople().set("$name", userName);
    }

    @Override
    public void onLoggedInPerformed(String deviceId, int uid, long phoneNumber, String userName) {
        saveDeviceId(deviceId);
        mixpanel.alias("uid:" + uid, "device:" + deviceId);
        mixpanel.identify("uid:" + uid);
        mixpanel.getPeople().identify("uid:" + uid);
        mixpanel.getPeople().set("$phone", phoneNumber);
        mixpanel.getPeople().set("$name", userName);
    }

    @Override
    public void trackEvent(String event) {
        JSONObject props = new JSONObject();
        mixpanel.track(event, props);
    }

    @Override
    public void trackEvent(String event, HashMap<String, String> hashMap) {
        JSONObject props = new JSONObject();
        for (String key : hashMap.keySet()) {
            try {
                props.put(key, hashMap.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mixpanel.track(event, props);
    }
}
