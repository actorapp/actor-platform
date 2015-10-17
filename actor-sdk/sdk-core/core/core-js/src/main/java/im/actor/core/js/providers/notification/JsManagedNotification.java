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

    public static native void show(String title, String message, String avatarUrl)/*-{

        var showNotification = function(title, body, icon) {
            var n = new Notification(title, {
                body: body,
                tag: 'new-message',
                icon: icon,
            });

            n.onclick = function() {
                $wnd.focus();
                this.close();
            }
        };

        var MAX_DEFER = 300;

        var deferStart = null;
        var deferredShow = null;

        var createNotification = function(title, body, icon) {
            if (deferredShow === null) {
                deferStart = Date.now();
            } else {
                clearTimeout(deferredShow)
            }

            if (Date.now() - deferStart > MAX_DEFER) {
                deferredShow = null;
                deferStart = null;
                showNotification(title, body, icon);
            } else {
                deferredShow = setTimeout(function () {
                    showNotification(title, body, icon)
                }, 300)
            }
        };

        createNotification(title, message, avatarUrl);
    }-*/;
}
