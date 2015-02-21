package im.actor.gwt.app.threading;

import im.actor.model.util.ThreadLocalCompat;

/**
 * Created by ex3ndr on 21.02.15.
 */
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
