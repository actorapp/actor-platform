/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import com.google.gwt.core.client.Scheduler;

import im.actor.runtime.DispatcherRuntime;

public class JsDispatcherProvider implements DispatcherRuntime {
    @Override
    public void dispatch(final Runnable runnable) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                runnable.run();
            }
        });
    }
}
