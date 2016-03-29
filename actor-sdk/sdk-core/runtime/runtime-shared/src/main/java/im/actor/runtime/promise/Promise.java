package im.actor.runtime.promise;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Supplier;

/**
 * Promise support implementations. It is much more like js promises than traditional
 * java/scala versions.
 *
 * @param <T> type of result
 */
public class Promise<T> {

    //
    // Dispatching parameters
    //

    private final ArrayList<PromiseCallback<T>> callbacks = new ArrayList<>();
    private final PromiseFunc<T> executor;
    private PromiseDispatcher dispatchActor;

    //
    // State of Promise
    //

    private volatile T result;
    private volatile Exception exception;
    private volatile boolean isFinished;
    private boolean isStarted;

    /**
     * Default constructor of promise
     */
    @ObjectiveCName("initWithExecutor:")
    public Promise(PromiseFunc<T> executor) {
        this.executor = executor;
    }

    /**
     * Internal constructor to work-around lambda support issueses
     */
    Promise() {
        this.executor = null;
    }

    /**
     * Handling successful result
     *
     * @param then supplier for result
     * @return this
     */
    @ObjectiveCName("then:")
    public synchronized Promise<T> then(final Consumer<T> then) {
        if (isFinished) {
            if (exception == null) {
                dispatchActor.dispatch(this, new Runnable() {
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
    @ObjectiveCName("failure:")
    public synchronized Promise<T> failure(final Consumer<Exception> failure) {
        if (isFinished) {
            if (exception != null) {
                dispatchActor.dispatch(this, new Runnable() {
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
    @ObjectiveCName("complete:")
    public synchronized Promise<T> complete(final PromiseCallback<T> callback) {
        if (isFinished) {
            dispatchActor.dispatch(this, new Runnable() {
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
    @ObjectiveCName("pipeTo:")
    public synchronized Promise<T> pipeTo(final PromiseResolver<T> resolver) {
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
     *
     * @param ref Scheduling actor
     * @return this
     */
    @ObjectiveCName("doneWithRef:")
    public Promise<T> done(ActorRef ref) {
        return done(new PromiseActorDispatcher(ref));
    }

    /**
     * Call this method to start promise execution
     *
     * @param dispatcher Scheduling dispatcher
     * @return this
     */
    @ObjectiveCName("done:")
    public Promise<T> done(PromiseDispatcher dispatcher) {
        if (isStarted) {
            // throw new RuntimeException("Promise already started!");
            return this;
        }
        isStarted = true;
        dispatchActor = dispatcher;
        dispatchActor.dispatch(this, new Runnable() {
            @Override
            public void run() {
                exec(new PromiseResolver<>(Promise.this, dispatchActor));
            }
        });
        return this;
    }

    @ObjectiveCName("log:")
    public Promise<T> log(final String TAG) {
        return complete(new PromiseCallback<T>() {
            @Override
            public void onResult(T t) {
                Log.d(TAG, "Result: " + t);
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Error: " + e);
            }
        });
    }

    /**
     * Main execution method
     *
     * @param resolver resolver
     */
    void exec(PromiseResolver<T> resolver) {
        executor.exec(resolver);
    }

    /**
     * Cast promise to different type
     *
     * @param <R> destination type
     * @return casted promise
     */
    @ObjectiveCName("cast")
    public <R> Promise<R> cast() {
        return (Promise<R>) this;
    }

    /**
     * Getting result if finished
     *
     * @return result
     */
    @ObjectiveCName("getResult")
    public T getResult() {
        if (!isFinished) {
            throw new RuntimeException("Promise is not finished!");
        }
        return result;
    }

    /**
     * Is promise finished
     *
     * @return result
     */
    @ObjectiveCName("isFinished")
    public boolean isFinished() {
        return isFinished;
    }

    /**
     * Exception if promise finished with error
     *
     * @return exception
     */
    @ObjectiveCName("getException")
    public Exception getException() {
        if (!isFinished) {
            throw new RuntimeException("Promise is not finished!");
        }
        return exception;
    }

    @ObjectiveCName("mapIfNull:")
    public Promise<T> mapIfNull(final Supplier<T> producer) {
        final Promise<T> self = this;
        return new Promise<T>(new PromiseFunc<T>() {
            @Override
            public void exec(final PromiseResolver<T> resolver) {
                self.then(new Consumer<T>() {
                    @Override
                    public void apply(T t) {
                        if (t == null) {
                            try {
                                t = producer.get();
                            } catch (Exception e) {
                                resolver.error(e);
                                return;
                            }
                            resolver.result(t);
                        } else {
                            resolver.result(t);
                        }
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
        });
    }

    @ObjectiveCName("mapIfNullPromise:")
    public Promise<T> mapIfNullPromise(final Supplier<Promise<T>> producer) {
        final Promise<T> self = this;
        return new Promise<T>(new PromiseFunc<T>() {
            @Override
            public void exec(final PromiseResolver<T> resolver) {
                self.then(new Consumer<T>() {
                    @Override
                    public void apply(T t) {
                        if (t == null) {
                            Promise<T> promise;
                            try {
                                promise = producer.get();
                            } catch (Exception e) {
                                resolver.error(e);
                                return;
                            }
                            promise.then(new Consumer<T>() {
                                @Override
                                public void apply(T t1) {
                                    resolver.result(t1);
                                }
                            });
                            promise.failure(new Consumer<Exception>() {
                                @Override
                                public void apply(Exception e) {
                                    resolver.error(e);
                                }
                            });
                            promise.done(resolver.getDispatcher());
                        } else {
                            resolver.result(t);
                        }
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
        });
    }

    /**
     * Mapping result value of promise to another value
     *
     * @param res mapping function
     * @param <R> destination type
     * @return promise
     */
    @ObjectiveCName("map:")
    public <R> Promise<R> map(final Function<T, R> res) {
        final Promise<T> self = this;
        return new Promise<R>() {
            @Override
            void exec(final PromiseResolver<R> resolver) {
                self.then(new Consumer<T>() {
                    @Override
                    public void apply(T t) {
                        R r;
                        try {
                            r = res.apply(t);
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
    }

    /**
     * Map result of promise to promise of value
     *
     * @param res mapping function
     * @param <R> destination type
     * @return promise
     */
    @ObjectiveCName("mapPromise:")
    public <R> Promise<R> mapPromise(final Function<T, Promise<R>> res) {
        final Promise<T> self = this;
        return new Promise<R>() {
            @Override
            void exec(final PromiseResolver<R> resolver) {
                self.then(new Consumer<T>() {
                    @Override
                    public void apply(T t) {
                        Promise<R> promise;
                        try {
                            promise = res.apply(t);
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

    public <R> Promise<T> mapPromiseSelf(final Function<T, Promise<R>> res) {
        return mapPromise(new Function<T, Promise<T>>() {
            @Override
            public Promise<T> apply(final T t) {
                return res.apply(t).map(new Function<R, T>() {
                    @Override
                    public T apply(R r) {
                        return t;
                    }
                });
            }
        });
    }

    @ObjectiveCName("fallback:")
    public Promise<T> fallback(final Function<Exception, Promise<T>> catchThen) {
        final Promise<T> self = this;
        return new Promise<T>(new PromiseFunc<T>() {
            @Override
            public void exec(final PromiseResolver<T> resolver) {
                self.then(new Consumer<T>() {
                    @Override
                    public void apply(T t) {
                        resolver.result(t);
                    }
                });
                self.failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        Promise<T> res = catchThen.apply(e);
                        res.then(new Consumer<T>() {
                            @Override
                            public void apply(T t) {
                                resolver.result(t);
                            }
                        });
                        res.failure(new Consumer<Exception>() {
                            @Override
                            public void apply(Exception e) {
                                resolver.error(e);
                            }
                        });
                        res.done(resolver.getDispatcher());
                    }
                });
                self.done(resolver.getDispatcher());
            }
        });
    }

    @ObjectiveCName("afterVoid:")
    public <R> Promise<R> afterVoid(final Supplier<Promise<R>> promiseSupplier) {
        final Promise<T> self = this;
        return new Promise<R>(new PromiseFunc<R>() {
            @Override
            public void exec(final PromiseResolver<R> resolver) {
                self.then(new Consumer<T>() {
                    @Override
                    public void apply(T t) {
                        Promise<R> promise = promiseSupplier.get();
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
                        resolver.error(e);
                    }
                });
                self.done(resolver.getDispatcher());
            }
        });
    }

    /**
     * Delivering result
     */
    private void deliverResult() {
        if (callbacks.size() > 0) {
            dispatchActor.dispatch(this, new Runnable() {
                @Override
                public void run() {
                    if (exception != null) {
                        for (PromiseCallback<T> callback : callbacks) {
                            try {
                                callback.onError(exception);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        for (PromiseCallback<T> callback : callbacks) {
                            try {
                                callback.onResult(result);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    callbacks.clear();
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
        deliverResult();
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