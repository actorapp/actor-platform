package com.droidkit.actors.dispatch;

/**
 * Used as callback for message processing
 */
public interface Dispatch<T> {
    void dispatchMessage(T message);
}
