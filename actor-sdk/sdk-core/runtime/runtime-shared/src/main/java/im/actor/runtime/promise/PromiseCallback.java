package im.actor.runtime.promise;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * Callback for retrieving result of promise
 *
 * @param <T> type of successful result
 */
public interface PromiseCallback<T> {
    @ObjectiveCName("onResult:")
    void onResult(T t);

    @ObjectiveCName("onError:")
    void onError(Exception e);
}