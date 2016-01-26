package im.actor.runtime.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Supplier;

/**
 * Promise support implementations. It is much more like js promises than traditional
 * java/scala versions.
 *
 * @param <T> type of result
 */
public abstract class Promise<T> {

    private final ArrayList<PromiseCallback<T>> callbacks = new ArrayList<PromiseCallback<T>>();

    private ActorRef dispatchActor;
    private volatile T result;
    private volatile Exception exception;
    private volatile boolean isFinished;
    private boolean isStarted;

    /**
     * Default constructor of promise
     */
    public Promise() {

    }

    /**
     * Handling successful result
     *
     * @param then supplier for result
     * @return this
     */
    public synchronized Promise<T> then(final Supplier<T> then) {
        if (isFinished) {
            if (exception == null) {
                dispatchActor.send(new Runnable() {
                    @Override
                    public void run() {
                        then.apply(result);
                    }
                });
            }
        } else {
            callbacks.add(new PromiseCallback<T>() {
                @Override
                public void onResult(T t) {
                    then.apply(t);
                }

                @Override
                public void onError(Exception e) {
                    // Do nothing
                }
            });
        }
        return this;
    }

    /**
     * Handling failure
     *
     * @param failure supplier for exception
     * @return this
     */
    public synchronized Promise<T> failure(final Supplier<Exception> failure) {
        if (isFinished) {
            if (exception != null) {
                dispatchActor.send(new Runnable() {
                    @Override
                    public void run() {
                        failure.apply(exception);
                    }
                });
            }
        } else {
            callbacks.add(new PromiseCallback<T>() {
                @Override
                public void onResult(T t) {
                    // Do nothing
                }

                @Override
                public void onError(Exception e) {
                    failure.apply(e);
                }
            });
        }
        return this;
    }

    /**
     * Handling complete
     *
     * @param callback callback for completion
     * @return this
     */
    public synchronized Promise<T> complete(final PromiseCallback<T> callback) {
        if (isFinished) {

            dispatchActor.send(new Runnable() {
                @Override
                public void run() {
                    if (exception != null) {
                        callback.onError(exception);
                    } else {
                        callback.onResult(result);
                    }
                }
            });
        } else {
            callbacks.add(callback);
        }
        return this;
    }

//    /**
//     * Binding result dispatching to actor
//     *
//     * @param ref dest actor
//     * @return this
//     */
//    public Promise<T> dispatch(ActorRef ref) {
//        dispatcher = PromiseDispatcher.forActor(ref);
//        return this;
//    }

    /**
     * Call this method to start promise execution
     */
    public Promise<T> done(ActorRef ref) {
        if (isStarted) {
            throw new RuntimeException("Promise already started");
        }
        isStarted = true;
        dispatchActor = ref;
        exec(new PromiseResolver<T>(this));
        return this;
    }

    public <R> Promise<R> cast() {
        return (Promise<R>) this;
    }

    public ActorRef getDispatchActor() {
        return dispatchActor;
    }

    //    public <R> Promise<R> zip(ArrayFunction<T, R> zip) {
//        return Promises.zip((Promise<T[]>) this, zip);
//    }

//    /**
//     * Getting current dispatcher for promise
//     *
//     * @return current dispatcher
//     */
//    public PromiseDispatcher getDispatcher() {
//        return dispatcher;
//    }

    public boolean isFinished() {
        return isFinished;
    }

    public Exception getException() {
        return exception;
    }

    public T getResult() {
        return result;
    }

    /**
     * Subclasses need to implement exec method for starting execution
     *
     * @param executor object that is used for result delivering
     */
    protected abstract void exec(@NotNull PromiseResolver<T> executor);

    /**
     * Delivering result
     */
    private void deliverResult() {
        if (callbacks.size() > 0) {
            im.actor.runtime.Runtime.dispatch(new Runnable() {
                @Override
                public void run() {
                    if (exception != null) {
                        for (PromiseCallback<T> callback : callbacks) {
                            callback.onError(exception);
                        }
                    } else {
                        for (PromiseCallback<T> callback : callbacks) {
                            callback.onResult(result);
                        }
                    }
                }
            });
        }

    }

    /**
     * Called when promise ended with error
     *
     * @param e error
     */
    synchronized void error(@NotNull Exception e) {
        if (isFinished) {
            throw new RuntimeException("Promise already completed!");
        }
        if (e == null) {
            throw new RuntimeException("Error can't be null");
        }
        isFinished = true;
        exception = e;
        deliverResult();
    }

    /**
     * Called when result is ready
     *
     * @param res result
     */
    synchronized void result(@Nullable T res) {
        if (isFinished) {
            throw new RuntimeException("Promise already completed!");
        }
        isFinished = true;
        result = res;
        deliverResult();
    }
}