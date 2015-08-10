/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * Provider for Log support
 */
public interface LogRuntime {
    /**
     * Log warring message
     *
     * @param tag     tag of message
     * @param message message content
     */
    @ObjectiveCName("warringWithTag:withMessage:")
    void w(String tag, String message);

    /**
     * Log exception
     *
     * @param tag       tag of exception
     * @param throwable exception
     */
    @ObjectiveCName("errorWithTag:withThrowable:")
    void e(String tag, Throwable throwable);

    /**
     * Log debug message
     *
     * @param tag     tag of message
     * @param message message content
     */
    @ObjectiveCName("debugWithTag:withMessage:")
    void d(String tag, String message);

    /**
     * Log verbose message
     *
     * @param tag     tag of message
     * @param message message content
     */
    @ObjectiveCName("verboseWithTag:withMessage:")
    void v(String tag, String message);
}
