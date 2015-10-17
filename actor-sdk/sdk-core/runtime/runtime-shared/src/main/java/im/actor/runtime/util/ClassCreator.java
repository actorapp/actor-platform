package im.actor.runtime.util;

/**
 * Interface for creating objects without reflection
 *
 * @param <T> type of instance
 */
public interface ClassCreator<T> {

    /**
     * Create instance of object
     *
     * @return created object
     */
    T newInstance();
}
