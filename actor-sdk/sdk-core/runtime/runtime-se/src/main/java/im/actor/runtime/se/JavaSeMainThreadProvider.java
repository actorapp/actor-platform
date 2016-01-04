/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.se;

import im.actor.runtime.MainThreadRuntime;

public class JavaSeMainThreadProvider implements MainThreadRuntime {

    @Override
    public void postToMainThread(Runnable runnable) {
        // TODO: Implement correctly
        runnable.run();
    }

    @Override
    public boolean isMainThread() {
        // TODO: Implement correctly
        return true;
    }

    @Override
    public boolean isSingleThread() {
        return false;
    }
}
