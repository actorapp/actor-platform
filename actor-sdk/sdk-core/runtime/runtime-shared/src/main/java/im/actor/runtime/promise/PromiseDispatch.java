package im.actor.runtime.promise;

/**
 * Actor message that is used in Actor scheduling
 */
public abstract class PromiseDispatch implements Runnable {

    private Promise promise;

    public PromiseDispatch(Promise promise) {
        this.promise = promise;
    }

    public Promise getPromise() {
        return promise;
    }
}
