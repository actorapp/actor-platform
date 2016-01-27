package im.actor.runtime.promise;

public abstract class PromiseDispatch implements Runnable {

    private Promise promise;

    public PromiseDispatch(Promise promise) {
        this.promise = promise;
    }

    public Promise getPromise() {
        return promise;
    }
}
