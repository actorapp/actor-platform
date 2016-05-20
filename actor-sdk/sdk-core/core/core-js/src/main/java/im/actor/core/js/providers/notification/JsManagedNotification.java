/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers.notification;

public class JsManagedNotification {

    public static native boolean isGranted()/*-{
        if (Notification.permission !== "granted") {
            Notification.requestPermission();
            return false;
        }
        return true;
    }-*/;

    public static native void show(String key, String title, String message, String avatarUrl)/*-{

        var showNotification = function(key, title, body, icon) {
            var n = new Notification(title, {
                body: body,
                tag: 'new-message',
                icon: icon
            });
            n.onclick = function() {
                $wnd.focus();
                if (key !== null) {
                    $wnd.location.replace('#/im/' + key);
                }
                this.close();
            }
        };

        var MAX_DEFER = 300;

        var deferStart = null;
        var deferredShow = null;

        var createNotification = function(key, title, body, icon) {
            if (deferredShow === null) {
                deferStart = Date.now();
            } else {
                clearTimeout(deferredShow)
            }

            if (Date.now() - deferStart > MAX_DEFER) {
                deferredShow = null;
                deferStart = null;
                showNotification(key, title, body, icon);
            } else {
                deferredShow = setTimeout(function () {
                    showNotification(key, title, body, icon)
                }, 300)
            }
        };

        createNotification(key, title, message, avatarUrl);
    }-*/;
}
