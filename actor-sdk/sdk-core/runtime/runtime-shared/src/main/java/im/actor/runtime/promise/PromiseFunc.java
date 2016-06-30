package im.actor.runtime.promise;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

/**
 * Method that evaluate result of a promise
 *
 * @param <T> result type
 */
public interface PromiseFunc<T> {

    @ObjectiveCName("exec:")
    void exec(@NotNull PromiseResolver<T> resolver);
}
