/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.util.HashMap;

import im.actor.model.AnalyticsProvider;
import im.actor.model.entity.Peer;

public class Analytics extends BaseModule {

    private static final String EVENT_APP_VISIBLE = "App Visible";
    private static final String EVENT_APP_HIDDEN = "App Hidden";

    private static final String DIALOGS_OPEN = "Dialogs Open";
    private static final String DIALOGS_CLOSED = "Dialogs Closed";

    private static final String CHAT_OPEN = "Chat Open";
    private static final String CHAT_CLOSED = "Chat Closed";

    private static final String PROFILE_OPEN = "Profile Open";
    private static final String PROFILE_CLOSED = "Profile Closed";

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

    public void onDialogsOpen() {
        track(DIALOGS_OPEN);
    }

    public void onDialogsClosed() {
        track(DIALOGS_CLOSED);
    }

    public void onChatOpen(Peer peer) {
        track(CHAT_OPEN, "Type", peer.getPeerType().toString(), "Id", peer.getPeerId() + "");
    }

    public void onChatClosed(Peer peer) {
        track(CHAT_CLOSED, "Type", peer.getPeerType().toString(), "Id", peer.getPeerId() + "");
    }

    public void onProfileOpen(int uid) {
        track(PROFILE_OPEN, "Id", uid + "");
    }

    public void onProfileClosed(int uid) {
        track(PROFILE_CLOSED, "Id", uid + "");
    }

    public void trackAppVisible() {
        track(EVENT_APP_VISIBLE);
    }

    public void trackAppHidden() {
        track(EVENT_APP_HIDDEN);
    }

    public void trackAuthPhoneOpen() {
        track("Auth: Phone Opened");
    }

    public void trackAuthCountryOpen() {
        track("Auth: Phone Country Opened");
    }

    public void trackAuthCountryClosed() {
        track("Auth: Phone Country Closed");
    }

    public void trackAuthCountryPicked(String country) {
        track("Auth: Phone Country Picked", "Country", country);
    }

    public void trackAuthPhoneType(String newValue) {
        track("Auth: Phone Typed: " + newValue);
    }

    public void trackAuthPhoneInfoOpen() {
        track("Auth: Phone Info Opened");
    }

    public void trackCodeRequest() {
        track("Auth: Press code request");
    }

    public void trackCodeRequest(long phone) {
        track("Auth: Code request", "Phone", phone + "");
    }

    public void trackActionError(String action, String tag, String message) {
        track(action + " error", "Tag", tag, "Message", message);
    }

    public void trackActionSuccess(String action) {
        track(action + " success");
    }

    public void trackActionTryAgain(String action) {
        track(action + " try again");
    }

    public void trackActionCancel(String action) {
        track(action + " cancel");
    }

    public void track(String event) {
        if (analyticsProvider != null) {
            analyticsProvider.trackEvent(event);
        }
    }

    public void track(String event, String... args) {
        if (analyticsProvider != null) {
            HashMap<String, String> params = new HashMap<String, String>();
            for (int i = 0; i < args.length; i += 2) {
                params.put(args[i], args[i + 1]);
            }
            analyticsProvider.trackEvent(event, params);
        }
    }
}
