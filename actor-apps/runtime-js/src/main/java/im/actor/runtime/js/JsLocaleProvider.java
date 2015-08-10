/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import im.actor.runtime.LocaleRuntime;

public class JsLocaleProvider implements LocaleRuntime {

    @Override
    public String getCurrentLocale() {
        return null;
    }

    @Override
    public String formatDate(long date) {
        return formatDate(date / 1000);
    }

    @Override
    public String formatTime(long date) {
        return formatTime(date / 1000);
    }

    private final native String formatDate(int dateVal)/*-{
        var date = new Date(dateVal * 1000);
        return date.toLocaleDateString()
    }-*/;

    // TODO: Reimplement
    private final native String formatTime(int dateVal)/*-{
        var date = new Date(dateVal * 1000);
        return date.toLocaleTimeString()
    }-*/;
}
