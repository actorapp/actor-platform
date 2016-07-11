/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android;

import im.actor.runtime.DispatcherRuntime;
import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.android.threading.AndroidImmediateDispatcher;

public class AndroidDispatcherProvider implements DispatcherRuntime {

    private AndroidImmediateDispatcher dispatcher;

    public AndroidDispatcherProvider() {

    }

    @Override
    public synchronized void dispatch(Runnable runnable) {
        if (dispatcher == null) {
            dispatcher = new AndroidImmediateDispatcher("callback_dispatcher", ThreadPriority.LOW);
        }
        dispatcher.dispatchNow(runnable);
    }
}
