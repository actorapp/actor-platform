package im.actor.runtime.promise;

public abstract class PromiseDispatcher {
    public abstract void dispatch(Promise promise, Runnable runnable);
}
