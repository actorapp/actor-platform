package im.actor.runtime.threading;

/**
 * Compatible Weak Reference
 *
 * @param <T> type of reference
 */
public abstract class WeakReferenceCompat<T> {
    public abstract T get();
}