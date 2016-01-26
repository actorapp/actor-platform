package im.actor.runtime.actors.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Object for completing promises
 *
 * @param <T>
 */
public class PromiseExecutor<T> {

    private Promise<T> promise;

    PromiseExecutor(Promise<T> promise) {
        this.promise = promise;
    }
    
    /**
     * Call this to complete promise
     *
     * @param res result of promise
     */
    public void result(@Nullable T res) {
        promise.result(res);
    }

    /**
     * Call this to fail promise
     *
     * @param e reason
     */
    public void error(@NotNull Exception e) {
        promise.error(e);
    }
}
