package im.actor.runtime.promise;

/**
 * Callback for retrieving result of promise
 *
 * @param <T> type of successful result
 */
public interface PromiseCallback<T> {

    void onResult(T t);

    void onError(Exception e);
}