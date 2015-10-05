/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.dispatch;

/**
 * Used as callback for message processing
 */
public interface Dispatch<T> {
    void dispatchMessage(T message);
}
