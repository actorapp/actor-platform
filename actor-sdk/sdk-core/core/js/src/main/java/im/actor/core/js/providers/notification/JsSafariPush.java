/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers.notification;

public class JsSafariPush {
    public static native boolean isSupported()/*-{
        return 'safari' in $wnd && 'pushNotification' in $wnd.safari
    }-*/;

    public static native void subscribe()/*-{
        var checkRemotePermission = function (permissionData) {
            if (permissionData.permission === 'default') {
            // This is a new web service URL and its validity is unknown.
            window.safari.pushNotification.requestPermission(
                'https://app.actor.im', // The web service URL.
                'web.com.example.domain',     // The Website Push ID.
                {}, // Data that you choose to send to your server to help you identify the user.
                checkRemotePermission         // The callback function.
            );
        }
        else if (permissionData.permission === 'denied') {
            // The user said no.
        }
        else if (permissionData.permission === 'granted') {
            // The web service URL is a valid push provider, and the user said yes.
            // permissionData.deviceToken is now available to use.
        }

        var permissionData = $wnd.safari.pushNotification.permission('web.com.example.domain');
        checkRemotePermission(permissionData);
    };

    }-*/;
}