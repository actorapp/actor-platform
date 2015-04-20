package im.actor.model.js.providers;

import com.google.gwt.core.client.Scheduler;

import im.actor.model.MainThreadProvider;

/**
 * Created by ex3ndr on 21.02.15.
 */
public class JsMainThreadProvider implements MainThreadProvider {

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
