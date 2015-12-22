/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import com.google.gwt.i18n.client.LocaleInfo;

import im.actor.runtime.LocaleRuntime;
import im.actor.runtime.Log;

public class JsLocaleProvider implements LocaleRuntime {

    @Override
    public String getCurrentLocale() {
        String locale = LocaleInfo.getCurrentLocale().getLocaleName();
        if (locale == null) {
            Log.d("JsLocaleProvider", "Found Null. Returning En");
            return "En";
        }
        if ("default".equals(locale)) {
            Log.d("JsLocaleProvider", "Found default. Returning En");
            return "En";
        }
        if (locale.length() >= 2) {
            String res = locale.substring(0, 1).toUpperCase() + locale.substring(1, 2).toLowerCase();
            Log.d("JsLocaleProvider", "Found " + res);
            return res;
        }
        Log.d("JsLocaleProvider", "Found unknown: " + locale + ". Returning En.");
        return "En";
    }

    @Override
    public String formatDate(long date) {
        return formatDateNative((int) (date / 1000));
    }

    @Override
    public String formatTime(long date) {
        return formatTimeNative((int) (date / 1000));
    }

    private native String formatDateNative(int dateVal)/*-{
        var date = new Date(dateVal * 1000);
        return date.toLocaleDateString();
    }-*/;

    // TODO: 24/12 hour format handling
    private native String formatTimeNative(int dateVal)/*-{
        var d = new Date(dateVal * 1000);
        var hr = d.getHours();
        var min = d.getMinutes();
        if (min < 10) {
            min = "0" + min;
        }
        return hr + ":" + min;
    }-*/;
}
