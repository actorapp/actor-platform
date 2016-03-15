package im.actor.runtime.js.threading;

import im.actor.runtime.threading.WeakReferenceCompat;

public class JsWeakReference<T> extends WeakReferenceCompat<T> {

    private T value;

    public JsWeakReference(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }
}
