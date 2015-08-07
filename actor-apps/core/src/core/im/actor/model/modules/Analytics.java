/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.util.HashMap;

import im.actor.model.AnalyticsProvider;
import im.actor.model.entity.Peer;

public class Analytics extends BaseModule {

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
        if (analyticsProvider != null && phoneNumbers.length>0) {
            analyticsProvider.onLoggedIn(deviceId, uid, phoneNumbers[0], userName);
        }
    }

    public void onLoggedInPerformed(String deviceId, int uid, Long[] phoneNumber, String userName) {
        if (analyticsProvider != null && phoneNumber.length>0) {
            analyticsProvider.onLoggedInPerformed(deviceId, uid, phoneNumber[0], userName);
        }
    }

    public void trackMainScreensOpen() {
        track("Main Screen Open");
    }

    public void trackMainScreensClosed() {
        track("Main Screen Closed");
    }

    public void trackOwnProfileOpen() {
        track("Own Profile Open");
    }

    public void trackOwnProfileClosed() {
        track("Own Profile Closed");
    }

    public void trackDialogsOpen() {
        track("Dialogs Open");
    }

    public void trackDialogsClosed() {
        track("Dialogs Closed");
    }

    public void trackContactsOpen() {
        track("Contacts Open");
    }

    public void trackTextSend(Peer peer) {
        track("Text Send", "Type", peer.getPeerType().toString(), "Id", peer.getPeerId() + "");
    }

    public void trackPhotoSend(Peer peer) {
        track("Photo Send", "Type", peer.getPeerType().toString(), "Id", peer.getPeerId() + "");
    }

    public void trackVideoSend(Peer peer) {
        track("Video Send", "Type", peer.getPeerType().toString(), "Id", peer.getPeerId() + "");
    }

    public void trackDocumentSend(Peer peer) {
        track("Document Send", "Type", peer.getPeerType().toString(), "Id", peer.getPeerId() + "");
    }

    public void trackContactsClosed() {
        track("Contacts Closed");
    }

    public void trackChatOpen(Peer peer) {
        track("Chat Open", "Type", peer.getPeerType().toString(), "Id", peer.getPeerId() + "");
    }

    public void trackChatClosed(Peer peer) {
        track("Chat Closed", "Type", peer.getPeerType().toString(), "Id", peer.getPeerId() + "");
    }

    public void trackProfileOpen(int uid) {
        track("Profile Open", "Id", uid + "");
    }

    public void trackProfileClosed(int uid) {
        track("Profile Closed", "Id", uid + "");
    }

    // Viral

    public void trackInvitePressed() {
        track("Invite pressed");
    }

    public void trackAddContactPressed() {
        track("Add contact pressed");
    }

    // Common

    public void trackAppVisible() {
        track("App Visible");
    }

    public void trackAppHidden() {
        track("App Hidden");
    }

    // Auth

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
        track("Auth: Phone Typed", "Value", newValue);
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

    // Auth code

    public void trackAuthCodeOpen() {
        track("Auth: Code Opened");
    }

    public void trackAuthCodeClosed() {
        track("Auth: Code Closed");
    }

    public void trackAuthCodeType(String newValue) {
        track("Auth: Code Typed", "Value", newValue);
    }

    public void trackAuthCodeWrongNumber() {
        track("Auth: Wrong number pressed");
    }

    public void trackAuthCodeWrongNumberCancel() {
        track("Auth: Wrong number cancel");
    }

    public void trackAuthCodeWrongNumberChange() {
        track("Auth: Wrong number change number");
    }

    // Auth signup

    public void trackAuthSignupOpen() {
        track("Auth: Signup Opened");
    }

    public void trackAuthSignupClosed() {
        track("Auth: Signup Closed");
    }

    public void trackAuthSignupClosedNameType(String newValue) {
        track("Auth: Name Typed", "Value", newValue);
    }

    public void trackAuthSignupPressedAvatar() {
        track("Auth: Picking avatar");
    }

    public void trackAuthSignupAvatarPicked() {
        track("Auth: Avatar picked");
    }

    public void trackAuthSignupAvatarDeleted() {
        track("Auth: Avatar deleted");
    }

    public void trackAuthSignupAvatarCanelled() {
        track("Auth: Avatar cancelled");
    }

    // Auth success

    public void trackAuthSuccess() {
        track("Auth: Completed");
    }

    // Activity

    public void trackBackPressed() {
        track("Auth: Back pressed");
    }

    public void trackUpPressed() {
        track("Auth: Up pressed");
    }

    // Actions

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
