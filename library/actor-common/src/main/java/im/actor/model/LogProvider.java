/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

/**
 * Provider for Log support
 */
public interface LogProvider {
    /**
     * Log warring message
     *
     * @param tag     tag of message
     * @param message message content
     */
    public void w(String tag, String message);

    /**
     * Log exception
     *
     * @param tag       tag of exception
     * @param throwable exception
     */
    public void e(String tag, Throwable throwable);

    /**
     * Log debug message
     *
     * @param tag     tag of message
     * @param message message content
     */
    public void d(String tag, String message);

    /**
     * Log verbose message
     *
     * @param tag     tag of message
     * @param message message content
     */
    public void v(String tag, String message);
}
