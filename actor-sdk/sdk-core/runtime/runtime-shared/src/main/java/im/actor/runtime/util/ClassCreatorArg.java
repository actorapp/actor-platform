package im.actor.runtime.util;

/**
 * Interface for creating objects with one argument without reflection
 *
 * @param <T>  type of object
 * @param <A1> type of argument
 */
public interface ClassCreatorArg<T, A1> {

    /**
     * Create object instance
     *
     * @param arg constructor argument
     * @return creted object
     */
    T newInstance(A1 arg);
}
