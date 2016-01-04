/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.se;

import im.actor.runtime.DispatcherRuntime;

public class JavaSeDispatcherProvider implements DispatcherRuntime {

    public JavaSeDispatcherProvider() {

    }

    @Override
    public void dispatch(Runnable runnable) {
        // TODO: Implement correctly
        runnable.run();
    }
}
