/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.threading;

/**
 * Abstract ThreadLocal implementation
 */
public abstract class ThreadLocalCompat<T> {
    /**
     * Return value for current Thread
     *
     * @return value
     */
    public abstract T get();

    /**
     * Set value for current Thread
     *
     * @param v new value
     */
    public abstract void set(T v);

    /**
     * Remove value for current Thread
     */
    public abstract void remove();
}
