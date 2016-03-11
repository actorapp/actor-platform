/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers.notification;

import com.google.gwt.core.client.JavaScriptObject;

public class JsNotification extends JavaScriptObject {

    public static native boolean isSupported()/*-{
        if (Notification) {
            return true;
        } else {
            return false;
        }
    }-*/;

    public static native boolean isGranted()/*-{
        if (Notification.permission !== "granted") {
            Notification.requestPermission();
            return false;
        }
        return true;
    }-*/;

    public static native JsNotification create(String key, String title, String message, String avatar)/*-{
        return new Notification(title, { body: message, icon: avatar });
    }-*/;

    protected JsNotification() {

    }

    public final native void close()/*-{
        this.close();
    }-*/;
}
