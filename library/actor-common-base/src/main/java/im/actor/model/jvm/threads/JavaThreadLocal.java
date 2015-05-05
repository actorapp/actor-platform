/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.jvm.threads;

import im.actor.model.util.ThreadLocalCompat;

public class JavaThreadLocal<T> extends ThreadLocalCompat<T> {
    private final ThreadLocal<T> tThreadLocal = new ThreadLocal<T>();

    @Override
    public T get() {
        return tThreadLocal.get();
    }

    @Override
    public void set(T v) {
        tThreadLocal.set(v);
    }

    @Override
    public void remove() {
        tThreadLocal.remove();
    }
}
