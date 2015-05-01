package im.actor.android;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import im.actor.model.AnalyticsProvider;

/**
 * Created by ex3ndr on 30.04.15.
 */
public class AndroidMixpanelAnalytics implements AnalyticsProvider {

    private MixpanelAPI mixpanel;
    private String deviceId;

    public AndroidMixpanelAnalytics(Context context, String token) {
        this.mixpanel = MixpanelAPI.getInstance(context, token);
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
    }

    @Override
    public void onLoggedIn(String deviceId, int uid, long phoneNumber, String userName) {
        saveDeviceId(deviceId);
        mixpanel.identify("uid:" + uid);
        mixpanel.getPeople().identify("uid:" + uid);
        mixpanel.getPeople().set("PhoneNumber", phoneNumber);
        mixpanel.getPeople().set("Name", userName);
    }

    @Override
    public void onLoggedInPerformed(String deviceId, int uid, long phoneNumber, String userName) {
        saveDeviceId(deviceId);
        mixpanel.identify("uid:" + uid);
        mixpanel.alias("uid:" + uid, "device:" + deviceId);
        mixpanel.getPeople().identify("uid:" + uid);
        mixpanel.getPeople().set("PhoneNumber", phoneNumber);
        mixpanel.getPeople().set("Name", userName);
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
