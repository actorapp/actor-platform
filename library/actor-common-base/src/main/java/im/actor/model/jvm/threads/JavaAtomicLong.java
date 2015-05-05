/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.jvm.threads;

import java.util.concurrent.atomic.AtomicLong;

import im.actor.model.util.AtomicLongCompat;

public class JavaAtomicLong extends AtomicLongCompat {
    final AtomicLong atomicLong;

    public JavaAtomicLong(long value) {
        atomicLong = new AtomicLong(value);
    }

    @Override
    public long get() {
        return atomicLong.get();
    }

    @Override
    public long incrementAndGet() {
        return atomicLong.incrementAndGet();
    }

    @Override
    public long getAndIncrement() {
        return atomicLong.getAndIncrement();
    }

    @Override
    public void set(long v) {
        atomicLong.set(v);
    }
}
