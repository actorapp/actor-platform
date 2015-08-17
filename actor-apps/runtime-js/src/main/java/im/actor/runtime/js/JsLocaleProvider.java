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
        Log.d("JsLocaleProvider", "Locale Name: " + LocaleInfo.getCurrentLocale().getLocaleName());
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
