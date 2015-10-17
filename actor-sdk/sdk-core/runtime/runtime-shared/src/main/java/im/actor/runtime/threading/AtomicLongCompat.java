/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.threading;

/**
 * Abstract Atomic Long
 */
public abstract class AtomicLongCompat {
    /**
     * Get Long value
     *
     * @return the value
     */
    public abstract long get();

    /**
     * Increment and get value
     *
     * @return incremented value
     */
    public abstract long incrementAndGet();

    /**
     * Get value and increment
     *
     * @return value before increment
     */
    public abstract long getAndIncrement();

    /**
     * Set Value
     *
     * @param v new value
     */
    public abstract void set(long v);
}
