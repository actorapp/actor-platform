/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.threading;

import im.actor.runtime.threading.ThreadLocalCompat;

public class GenericThreadLocal<T> extends ThreadLocalCompat<T> {
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
