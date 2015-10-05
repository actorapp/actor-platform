/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.threading;

import java.util.concurrent.atomic.AtomicInteger;

import im.actor.runtime.threading.AtomicIntegerCompat;

public class GenericAtomicInteger extends AtomicIntegerCompat {

    private final AtomicInteger atomicInteger;

    public GenericAtomicInteger(int value) {
        atomicInteger = new AtomicInteger(value);
    }

    @Override
    public int get() {
        return atomicInteger.get();
    }

    @Override
    public int incrementAndGet() {
        return atomicInteger.incrementAndGet();
    }

    @Override
    public int getAndIncrement() {
        return atomicInteger.getAndIncrement();
    }

    @Override
    public void compareAndSet(int exp, int v) {
        atomicInteger.compareAndSet(exp, v);
    }

    @Override
    public void set(int v) {
        atomicInteger.set(v);
    }
}
