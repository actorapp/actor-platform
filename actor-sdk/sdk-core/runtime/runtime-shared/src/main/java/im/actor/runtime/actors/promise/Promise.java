package im.actor.runtime.actors.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.runtime.function.Supplier;

/**
 * Promise support implementations. It is much more like js promises than traditional
 * java/scala versions.
 *
 * @param <T> type of result
 */
public abstract class Promise<T> {

    private final ArrayList<PromiseCallback<T>> callbacks = new ArrayList<PromiseCallback<T>>();

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
                im.actor.runtime.Runtime.dispatch(new Runnable() {
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
                im.actor.runtime.Runtime.dispatch(new Runnable() {
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

            im.actor.runtime.Runtime.dispatch(new Runnable() {
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

    /**
     * Call this method to start promise execution
     */
    public void done() {
        if (isStarted) {
            throw new RuntimeException("Promise already started");
        }
        isStarted = true;
        exec(new PromiseExecutor<T>(this));
    }

    /**
     * Subclasses need to implement exec method for starting execution
     *
     * @param executor object that is used for result delivering
     */
    protected abstract void exec(@NotNull PromiseExecutor<T> executor);

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


    /**
     * Combining sequence of promises to one single promise
     *
     * @param promises source promises
     * @param <T>      type of arguments
     * @return result promise
     */
    @SafeVarargs
    public static <T> Promise<T[]> sequence(final Promise<T>... promises) {
        if (promises.length == 0) {
            throw new RuntimeException("Promises array must not be empty");
        }
        return new Promise<T[]>() {
            @Override
            protected void exec(@NotNull final PromiseExecutor<T[]> executor) {
                final T[] res = (T[]) new Object[promises.length];
                final boolean[] isSet = new boolean[promises.length];
                final Promise self = this;
                for (int i = 0; i < res.length; i++) {
                    promises[i].done();
                    final int finalI = i;
                    promises[i].then(new Supplier<T>() {
                        @Override
                        public void apply(T t) {
                            if (self.isFinished) {
                                return;
                            }

                            res[finalI] = t;
                            isSet[finalI] = true;
                            for (int i = 0; i < promises.length; i++) {
                                if (!isSet[i]) {
                                    return;
                                }
                            }

                            executor.result(res);
                        }
                    }).failure(new Supplier<Exception>() {
                        @Override
                        public void apply(Exception e) {
                            if (self.isFinished) {
                                return;
                            }

                            executor.error(e);
                        }
                    });
                }
                for (Promise<T> p : promises) {
                    p.done();
                }
            }
        };
    }
}