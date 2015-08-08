/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers.threading;

import im.actor.core.util.AtomicLongCompat;

public class JsAtomicLong extends AtomicLongCompat {
    private long value;

    public JsAtomicLong(long value) {
        this.value = value;
    }

    @Override
    public long get() {
        return value;
    }

    @Override
    public long incrementAndGet() {
        return ++value;
    }

    @Override
    public long getAndIncrement() {
        return value++;
    }

    @Override
    public void set(long v) {
        value = v;
    }
}
