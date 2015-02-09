package com.droidkit.actors.utils;

/**
 * Created by ex3ndr on 09.02.15.
 */
public abstract class AtomicIntegerCompat {
    public abstract int get();

    public abstract int incrementAndGet();

    public abstract int getAndIncrement();

    public abstract void compareAndSet(int exp, int v);

    public abstract void set(int v);
}
