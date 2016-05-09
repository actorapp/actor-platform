/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.actor.runtime.DispatcherRuntime;

public class AndroidDispatcherProvider implements DispatcherRuntime {

    private Executor EXECUTOR;

    public AndroidDispatcherProvider() {

    }

    @Override
    public synchronized void dispatch(Runnable runnable) {
        if (EXECUTOR == null) {
            EXECUTOR = Executors.newSingleThreadExecutor();
        }
        EXECUTOR.execute(runnable);
    }
}
