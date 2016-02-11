package im.actor.runtime.promise;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;

/**
 * Object for completing promises
 *
 * @param <T> type of resolver
 */
public class PromiseResolver<T> {

    private Promise<T> promise;
    private ActorRef dispatcher;

    PromiseResolver(Promise<T> promise, ActorRef dispatcher) {
        this.promise = promise;
        this.dispatcher = dispatcher;
    }

    /**
     * Get Resolver's promise
     *
     * @return promise
     */
    @ObjectiveCName("getPromise")
    public Promise<T> getPromise() {
        return promise;
    }

    /**
     * Get Resolver's dispatcher
     *
     * @return dispatcher actor
     */
    @ObjectiveCName("getDispatcher")
    public ActorRef getDispatcher() {
        return dispatcher;
    }

    /**
     * Call this to complete promise
     *
     * @param res result of promise
     */
    @ObjectiveCName("result:")
    public void result(@Nullable T res) {
        promise.result(res);
    }

    /**
     * Trying to complete promise
     *
     * @param res result of promise
     */
    @ObjectiveCName("tryResult:")
    public void tryResult(@Nullable T res) {
        promise.tryResult(res);
    }

    /**
     * Call this to fail promise
     *
     * @param e reason
     */
    @ObjectiveCName("error:")
    public void error(@NotNull Exception e) {
        promise.error(e);
    }

    /**
     * Trying to fail promise
     *
     * @param e reason
     */
    @ObjectiveCName("tryError:")
    public void tryError(@NotNull Exception e) {
        promise.tryError(e);
    }

}
