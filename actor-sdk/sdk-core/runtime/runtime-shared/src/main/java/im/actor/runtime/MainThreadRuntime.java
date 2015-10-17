/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * Provider for dispatching on Main application Thread
 */
public interface MainThreadRuntime {
    /**
     * Post Runnable to main thread.
     * Implementation is recommended to always post to main thread
     * also in cases when method is called from main thread
     *
     * @param runnable Runnable to execute
     */
    @ObjectiveCName("postToMainThreadWithRunnable:")
    void postToMainThread(Runnable runnable);

    /**
     * Is current thread is main thread
     *
     * @return is main thread
     */
    @ObjectiveCName("isMainThread")
    boolean isMainThread();

    /**
     * Is current environment is single threaded (like javascript)
     *
     * @return is single threaded
     */
    @ObjectiveCName("isSingleThread")
    boolean isSingleThread();
}
