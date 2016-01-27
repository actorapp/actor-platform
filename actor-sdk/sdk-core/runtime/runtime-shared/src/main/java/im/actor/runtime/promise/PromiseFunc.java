package im.actor.runtime.promise;

/**
 * Method that evaluate result of a promise
 *
 * @param <T> result type
 */
public interface PromiseFunc<T> {

    void exec(PromiseResolver<T> executor);
}
