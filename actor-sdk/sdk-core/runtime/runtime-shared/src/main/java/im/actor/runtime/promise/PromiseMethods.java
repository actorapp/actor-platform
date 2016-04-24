package im.actor.runtime.promise;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.ConsumerDouble;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Supplier;

public interface PromiseMethods<T> {

    /**
     * Handling successful result
     *
     * @param then supplier for result
     * @return this
     */
    @ObjectiveCName("then:")
    Promise<T> then(final Consumer<T> then);

    /**
     * Handling failure
     *
     * @param failure supplier for exception
     * @return this
     */
    @ObjectiveCName("failure:")
    Promise<T> failure(final Consumer<Exception> failure);


    default Promise<T> complete(final ConsumerDouble<T, Exception> completeHandler) {
        then(t -> completeHandler.apply(t, null));
        failure(e -> completeHandler.apply(null, e));
        return (Promise<T>) this;
    }

    @ObjectiveCName("mapIfNull:")
    default Promise<T> mapIfNull(final Supplier<T> producer) {
        final Promise<T> self = (Promise<T>) this;
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
            }
        });
    }

    @ObjectiveCName("mapIfNullPromise:")
    default Promise<T> mapIfNullPromise(final Supplier<Promise<T>> producer) {
        final Promise<T> self = (Promise<T>) this;
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
    default <R> Promise<R> map(final Function<T, R> res) {
        final Promise<T> self = (Promise<T>) this;
        return new Promise<>(new PromiseFunc<R>() {
            @Override
            public void exec(@NotNull PromiseResolver<R> resolver) {
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
            }
        });
    }

    /**
     * Map result of promise to promise of value
     *
     * @param res mapping function
     * @param <R> destination type
     * @return promise
     */
    @ObjectiveCName("mapPromise:")
    default <R> Promise<R> mapPromise(final Function<T, Promise<R>> res) {
        final Promise<T> self = (Promise<T>) this;
        return new Promise<>(new PromiseFunc<R>() {
            @Override
            public void exec(@NotNull PromiseResolver<R> resolver) {
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
                    }
                });
                self.failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        resolver.tryError(e);
                    }
                });
            }
        });
    }

    default <R> Promise<T> mapPromiseSelf(final Function<T, Promise<R>> res) {
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
    default Promise<T> fallback(final Function<Exception, Promise<T>> catchThen) {
        final Promise<T> self = (Promise<T>) this;
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
                    }
                });
            }
        });
    }

    @ObjectiveCName("afterVoid:")
    default <R> Promise<R> afterVoid(final Supplier<Promise<R>> promiseSupplier) {
        final Promise<T> self = (Promise<T>) this;
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
                    }
                });
                self.failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        resolver.error(e);
                    }
                });
            }
        });
    }

}
