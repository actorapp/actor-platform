/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.js;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;

import org.timepedia.exporter.client.ExporterUtil;

public class AppEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        ExporterUtil.exportAll();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                onAppLoaded();
            }
        });
    }

    public native void onAppLoaded()/*-{
        if ($wnd.jsAppLoaded) $wnd.jsAppLoaded();
    }-*/;
}
