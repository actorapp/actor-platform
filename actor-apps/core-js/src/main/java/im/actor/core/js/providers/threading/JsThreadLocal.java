/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers.threading;

import im.actor.core.util.ThreadLocalCompat;

public class JsThreadLocal<T> extends ThreadLocalCompat<T> {
    T obj;

    @Override
    public T get() {
        return obj;
    }

    @Override
    public void set(T v) {
        this.obj = v;
    }

    @Override
    public void remove() {
        this.obj = null;
    }
}
