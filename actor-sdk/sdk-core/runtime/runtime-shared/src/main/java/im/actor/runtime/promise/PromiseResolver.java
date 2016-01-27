package im.actor.runtime.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;

/**
 * Object for completing promises
 *
 * @param <T>
 */
public class PromiseResolver<T> {

    private Promise<T> promise;

    PromiseResolver(Promise<T> promise) {
        this.promise = promise;
    }

    /**
     * Get Resolver's promise
     *
     * @return promise
     */
    public Promise<T> getPromise() {
        return promise;
    }

    public ActorRef getDispatcher() {
        return promise.getDispatchActor();
    }

    /**
     * Call this to complete promise
     *
     * @param res result of promise
     */
    public void result(@Nullable T res) {
        Log.d("Promise", "Result: " + promise);
        promise.result(res);
    }

    /**
     * Trying to complete promise
     *
     * @param res result of promise
     */
    public void tryResult(@Nullable T res) {
        promise.tryResult(res);
    }

    /**
     * Call this to fail promise
     *
     * @param e reason
     */
    public void error(@NotNull Exception e) {
        promise.error(e);
    }

    /**
     * Trying to fail promise
     *
     * @param e reason
     */
    public void tryError(@NotNull Exception e) {
        promise.tryError(e);
    }

}
