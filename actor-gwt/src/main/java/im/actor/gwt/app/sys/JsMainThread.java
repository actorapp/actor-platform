package im.actor.gwt.app.sys;

import com.google.gwt.core.client.Scheduler;

import im.actor.model.MainThread;

/**
 * Created by ex3ndr on 21.02.15.
 */
public class JsMainThread implements MainThread {
    @Override
    public void runOnUiThread(final Runnable runnable) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                runnable.run();
            }
        });
    }
}
