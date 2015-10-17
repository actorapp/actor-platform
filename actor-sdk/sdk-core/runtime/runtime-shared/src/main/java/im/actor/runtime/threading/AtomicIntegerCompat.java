/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.threading;

/**
 * Abstract Atomic Integer
 */
public abstract class AtomicIntegerCompat {
    /**
     * Get integer value
     *
     * @return the value
     */
    public abstract int get();

    /**
     * Increment and get value
     *
     * @return incremented value
     */
    public abstract int incrementAndGet();

    /**
     * Get value and increment
     *
     * @return value before increment
     */
    public abstract int getAndIncrement();

    /**
     * Set if value equals exp
     *
     * @param exp expected value
     * @param v   value
     */
    public abstract void compareAndSet(int exp, int v);

    /**
     * Set Value
     *
     * @param v new value
     */
    public abstract void set(int v);
}
