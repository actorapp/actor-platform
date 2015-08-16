/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * Provider for executing callbacks
 */
public interface DispatcherRuntime {

    /**
     * Dispatch Runnable on Callbacks thread
     *
     * @param runnable Runnable
     */
    @ObjectiveCName("dispatchWithRunnable:")
    void dispatch(Runnable runnable);
}
