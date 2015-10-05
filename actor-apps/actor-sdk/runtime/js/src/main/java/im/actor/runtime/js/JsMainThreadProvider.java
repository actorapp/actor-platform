/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import com.google.gwt.core.client.Scheduler;

import im.actor.runtime.MainThreadRuntime;

public class JsMainThreadProvider implements MainThreadRuntime {

    @Override
    public void postToMainThread(final Runnable runnable) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                runnable.run();
            }
        });
    }

    @Override
    public boolean isMainThread() {
        return true;
    }

    @Override
    public boolean isSingleThread() {
        return true;
    }
}
