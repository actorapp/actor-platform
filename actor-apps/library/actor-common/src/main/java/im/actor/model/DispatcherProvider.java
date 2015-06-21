/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * Provider for executing callbacks
 */
public interface DispatcherProvider {

    /**
     * Dispatch Runnable on Callbacks thread
     *
     * @param runnable Runnable
     */
    @ObjectiveCName("dispatchWithRunnable:")
    void dispatch(Runnable runnable);
}
