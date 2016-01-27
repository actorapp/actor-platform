package im.actor.runtime.promise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Map;

/**
 * Promise support implementations. It is much more like js promises than traditional
 * java/scala versions.
 *
 * @param <T> type of result
 */
public class Promise<T> {

    private final ArrayList<PromiseCallback<T>> callbacks = new ArrayList<PromiseCallback<T>>();
    private final PromiseFunc<T> executor;

    private ActorRef dispatchActor;
    private volatile T result;
    private volatile Exception exception;
    private volatile boolean isFinished;
    private boolean isStarted;

    /**
     * Default constructor of promise
     */
    public Promise(PromiseFunc<T> executor) {
        this.executor = executor;
    }

    Promise() {
        this.executor = null;
    }

    /**
     * Handling successful result
     *
     * @param then supplier for result
     * @return this
     */
    public synchronized Promise<T> then(final Consumer<T> then) {
        if (isFinished) {
            if (exception == null) {
                dispatchActor.send(new PromiseDispatch(this) {
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
    public synchronized Promise<T> failure(final Consumer<Exception> failure) {
        if (isFinished) {
            if (exception != null) {
                dispatchActor.send(new PromiseDispatch(this) {
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

            dispatchActor.send(new PromiseDispatch(this) {
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
     * Pipe result to resolver
     *
     * @param resolver destination resolver
     * @return this
     */
    public synchronized Promise<T> pipeTo(PromiseResolver<T> resolver) {
        complete(new PromiseCallback<T>() {
            @Override
            public void onResult(T t) {
                resolver.result(t);
            }

            @Override
            public void onError(Exception e) {
                resolver.error(e);
            }
        });
        return this;
    }

    /**
     * Call this method to start promise execution
     */
    public Promise<T> done(ActorRef ref) {
        Log.d("PromisesArray", "done " + this + " (" + ref.getPath() + ")");
        if (isStarted) {
            throw new RuntimeException("Promise already started");
        }
        isStarted = true;
        dispatchActor = ref;
        dispatchActor.send(new PromiseDispatch(this) {
            @Override
            public void run() {
                exec(new PromiseResolver<T>(Promise.this));
            }
        });
        return this;
    }

    void exec(PromiseResolver<T> resolver) {
        executor.exec(resolver);
    }

    public boolean isStarted() {
        return isStarted;
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

    public <R> Promise<R> map(Map<T, R> res) {
        final Promise<T> self = this;
        return new Promise<R>() {
            @Override
            void exec(PromiseResolver<R> resolver) {
                self.then(new Consumer<T>() {
                    @Override
                    public void apply(T t) {
                        R r;
                        try {
                            r = res.map(t);
                        } catch (Exception e) {
                            e.printStackTrace();
                            resolver.tryError(e);
                            return;
                        }
                        resolver.tryResult(r);
                    }
                });
                self.failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        resolver.error(e);
                    }
                });
                self.done(resolver.getDispatcher());
            }
        };
//        return new Promise<>(executor1 -> {
//            self.then(t -> {
//                R r;
//                try {
//                    r = res.map(t);
//                } catch (Exception e) {
//                    executor1.tryError(e);
//                    return;
//                }
//                executor1.tryResult(r);
//            });
//            self.failure(e -> executor1.error(e));
//            self.done(executor1.getDispatcher());
//        });
    }

    public <R> Promise<R> mapPromise(Map<T, Promise<R>> res) {
        final Promise<T> self = this;
        return new Promise<R>() {
            @Override
            void exec(final PromiseResolver<R> resolver) {
                self.then(new Consumer<T>() {
                    @Override
                    public void apply(T t) {
                        Promise<R> promise;
                        try {
                            promise = res.map(t);
                        } catch (Exception e) {
                            e.printStackTrace();
                            resolver.tryError(e);
                            return;
                        }

                        promise.then(new Consumer<R>() {
                            @Override
                            public void apply(R r) {
                                resolver.result(r);
                            }
                        });
                        promise.failure(new Consumer<Exception>() {
                            @Override
                            public void apply(Exception e) {
                                resolver.error(e);
                            }
                        });
                        promise.done(resolver.getDispatcher());
                    }
                });
                self.failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        resolver.tryError(e);
                    }
                });
                self.done(resolver.getDispatcher());
            }
        };
    }

    /**
     * Delivering result
     */
    private void deliverResult() {
        Log.d("Promise", "result:4");
        if (callbacks.size() > 0) {
            Log.d("Promise", "result:5");
            dispatchActor.send(new PromiseDispatch(this) {
                @Override
                public void run() {
                    Log.d("Promise", "result:6");
                    if (exception != null) {
                        Log.d("Promise", "result:7");
                        for (PromiseCallback<T> callback : callbacks) {
                            try {
                                Log.d("Promise", "result:callback:" + callback);
                                callback.onError(exception);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("Promise", "result:callback_error");
                            }
                        }
                    } else {
                        Log.d("Promise", "result:8");
                        for (PromiseCallback<T> callback : callbacks) {
                            try {
                                Log.d("Promise", "result:callback:" + callback);
                                callback.onResult(result);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("Promise", "result:callback_error2");
                            }
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
     * Trying complete promise with error
     *
     * @param e error
     */
    synchronized void tryError(@NotNull Exception e) {
        if (isFinished) {
            return;
        }
        error(e);
    }

    /**
     * Called when result is ready
     *
     * @param res result
     */
    synchronized void result(@Nullable T res) {
        if (isFinished) {
            throw new RuntimeException("Promise " + this + " already completed!");
        }
        isFinished = true;
        result = res;
        Log.d("Promise", "result:1");
        deliverResult();
        Log.d("Promise", "result:2");
    }

    /**
     * Trying complete promise with result
     *
     * @param res result
     */
    synchronized void tryResult(@Nullable T res) {
        if (isFinished) {
            return;
        }
        result(res);
    }
}