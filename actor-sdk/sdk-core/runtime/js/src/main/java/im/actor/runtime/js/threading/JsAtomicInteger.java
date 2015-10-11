/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.threading;

import im.actor.runtime.threading.AtomicIntegerCompat;

public class JsAtomicInteger extends AtomicIntegerCompat {

    private int value;

    public JsAtomicInteger(int value) {
        this.value = value;
    }

    @Override
    public int get() {
        return value;
    }

    @Override
    public int incrementAndGet() {
        return ++value;
    }

    @Override
    public int getAndIncrement() {
        return value++;
    }

    @Override
    public void compareAndSet(int exp, int v) {
        if (this.value == exp) {
            this.value = v;
        }
    }

    @Override
    public void set(int v) {
        this.value = v;
    }
}
