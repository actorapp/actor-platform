package im.actor.model.modules;

import im.actor.model.AnalyticsProvider;

/**
 * Created by ex3ndr on 30.04.15.
 */
public class Analytics extends BaseModule {

    private static final String EVENT_APP_VISIBLE = "app_visible";
    private static final String EVENT_APP_HIDDEN = "app_hidden";

    private AnalyticsProvider analyticsProvider;

    public Analytics(Modules modules) {
        super(modules);

        analyticsProvider = modules.getConfiguration().getAnalyticsProvider();
    }

    public void onLoggedOut(String deviceId) {
        if (analyticsProvider != null) {
            analyticsProvider.onLoggedOut(deviceId);
        }
    }

    public void onLoggedIn(String deviceId, int uid, Long[] phoneNumbers, String userName) {
        if (analyticsProvider != null) {
            analyticsProvider.onLoggedIn(deviceId, uid, phoneNumbers[0], userName);
        }
    }

    public void onLoggedInPerformed(String deviceId, int uid, Long[] phoneNumber, String userName) {
        if (analyticsProvider != null) {
            analyticsProvider.onLoggedInPerformed(deviceId, uid, phoneNumber[0], userName);
        }
    }

    public void trackAppVisible() {
        if (analyticsProvider != null) {
            analyticsProvider.trackEvent(EVENT_APP_VISIBLE);
        }
    }

    public void trackAppHidden() {
        if (analyticsProvider != null) {
            analyticsProvider.trackEvent(EVENT_APP_HIDDEN);
        }
    }
}
