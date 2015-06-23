/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import com.google.gwt.core.client.Scheduler;
import im.actor.model.DispatcherProvider;

public class JsDispatcherProvider implements DispatcherProvider {
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
