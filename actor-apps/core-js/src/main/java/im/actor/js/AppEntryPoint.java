/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.js;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.resources.client.ResourcePrototype;

import org.timepedia.exporter.client.ExporterUtil;

import im.actor.core.js.providers.Assets;
import im.actor.runtime.AssetsRuntimeProvider;
import im.actor.runtime.Log;

public class AppEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        ExporterUtil.exportAll();
        for (ResourcePrototype prototype : Assets.INSTANCE.getResources()) {
            Log.d("EntryPoint", "Resource: " + prototype.getName());
        }
        AssetsRuntimeProvider.registerBundle(Assets.INSTANCE);
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
