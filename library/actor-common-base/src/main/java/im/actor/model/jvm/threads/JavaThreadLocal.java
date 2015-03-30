package im.actor.model.jvm.threads;

import im.actor.model.util.ThreadLocalCompat;

/**
 * Created by ex3ndr on 16.02.15.
 */
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
