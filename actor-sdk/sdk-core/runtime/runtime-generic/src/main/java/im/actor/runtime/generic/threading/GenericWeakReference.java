package im.actor.runtime.generic.threading;

import java.lang.ref.WeakReference;

import im.actor.runtime.threading.WeakReferenceCompat;

public class GenericWeakReference<T> extends WeakReferenceCompat<T> {

    private WeakReference<T> weakReference;

    public GenericWeakReference(T val) {
        weakReference = new WeakReference<T>(val);
    }

    @Override
    public T get() {
        return weakReference.get();
    }
}
