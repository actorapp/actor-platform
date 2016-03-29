package im.actor.runtime.promise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import im.actor.runtime.Log;
import im.actor.runtime.RandomRuntime;
import im.actor.runtime.RandomRuntimeProvider;
import im.actor.runtime.function.ArrayFunction;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.ListFunction;
import im.actor.runtime.function.Predicate;
import im.actor.runtime.function.Predicates;

/**
 * Array of Promises. Allows you to invoke map, mapPromise and other useful methods
 * for manipulating data.
 *
 * @param <T> type of array
 */
public class PromisesArray<T> {
    private static final RandomRuntime rundom = new RandomRuntimeProvider();

    /**
     * Create PromisesArray from collection
     *
     * @param collection Source collection
     * @param <T>        type of array
     * @return array
     */
    @SuppressWarnings("unchecked")
    public static <T> PromisesArray<T> of(Collection<T> collection) {
        final ArrayList<Promise<T>> res = new ArrayList<>();
        for (T t : collection) {
            res.add(Promises.success(t));
        }
        final Promise<T>[] promises = (Promise<T>[]) res.toArray();
        return new PromisesArray<>(new PromiseFunc<Promise<T>[]>() {
            @Override
            public void exec(PromiseResolver<Promise<T>[]> executor) {
                executor.result(promises);
            }
        });
    }

    /**
     * Create PromisesArray from values
     *
     * @param items elements
     * @param <T>   type of array
     * @return array
     */
    @SafeVarargs
    public static <T> PromisesArray<T> of(T... items) {
        ArrayList<Promise<T>> res = new ArrayList<>();
        for (T t : items) {
            res.add(Promises.success(t));
        }
        final Promise[] promises = (Promise[]) res.toArray();
        return new PromisesArray<>(new PromiseFunc<Promise<T>[]>() {
            @Override
            public void exec(PromiseResolver<Promise<T>[]> executor) {
                executor.result(promises);
            }
        });
    }

    /**
     * Create PromisesArray from multiple Promise
     *
     * @param items promises
     * @param <T>   type of array
     * @return array
     */
    @SafeVarargs
    public static <T> PromisesArray<T> ofPromises(Promise<T>... items) {
        ArrayList<Promise<T>> res = new ArrayList<>();
        Collections.addAll(res, items);
        final Promise[] promises = res.toArray(new Promise[res.size()]);
        return new PromisesArray<>(new PromiseFunc<Promise<T>[]>() {
            @Override
            public void exec(PromiseResolver<Promise<T>[]> executor) {
                executor.result(promises);
            }
        });
    }

    public static <T> PromisesArray<T> ofPromises(Collection<Promise<T>> items) {
        ArrayList<Promise<T>> res = new ArrayList<>(items);
        // Collections.addAll(res, items);
        final Promise[] promises = res.toArray(new Promise[res.size()]);
        return new PromisesArray<>(new PromiseFunc<Promise<T>[]>() {
            @Override
            public void exec(PromiseResolver<Promise<T>[]> executor) {
                executor.result(promises);
            }
        });
    }

    //
    // Constructors and methods
    //

    private Promise<Promise<T>[]> promises;

    private PromisesArray(Promise<Promise<T>[]> promises) {
        this.promises = promises;
    }

    private PromisesArray(PromiseFunc<Promise<T>[]> executor) {
        this(new Promise<>(executor));
    }

    /**
     * Map promises results to new promises
     *
     * @param fun mapping function
     * @param <R> type of result promises
     * @return PromisesArray
     */
    public <R> PromisesArray<R> map(final Function<T, Promise<R>> fun) {
        return mapSourcePromises(new Function<Promise<T>, Promise<R>>() {
            @Override
            public Promise<R> apply(final Promise<T> srcPromise) {

                return new Promise<R>() {
                    @Override
                    void exec(final PromiseResolver<R> resolver) {

                        //                        //
                        // Handling results from source PromisesArray
                        //

                        srcPromise.then(new Consumer<T>() {
                            @Override
                            public void apply(T t) {

                                //
                                // Mapping value to promise
                                //
                                Promise<R> mapped = fun.apply(t);

                                //
                                // Handling results
                                //
                                mapped.then(new Consumer<R>() {
                                    @Override
                                    public void apply(R r) {
                                        resolver.result(r);
                                    }
                                }).failure(new Consumer<Exception>() {
                                    @Override
                                    public void apply(Exception e) {
                                        resolver.error(e);
                                    }
                                }).done(resolver.getDispatcher());
                            }
                        });

                        //
                        // Handling failures
                        //

                        srcPromise.failure(new Consumer<Exception>() {
                            @Override
                            public void apply(Exception e) {
                                resolver.error(e);
                            }
                        });

                        //
                        // Starting source promise
                        //

                        srcPromise.done(resolver.getDispatcher());
                    }
                };
            }
        });
    }

    public <R> PromisesArray<R> mapOptional(final Function<T, Promise<R>> fun) {
        return map(fun)
                .ignoreFailed()
                .filterNull();
    }

    public PromisesArray<T> ignoreFailed() {
        return mapSourcePromises(new Function<Promise<T>, Promise<T>>() {
            @Override
            public Promise<T> apply(final Promise<T> tPromise) {
                return new Promise<T>(new PromiseFunc<T>() {
                    @Override
                    public void exec(final PromiseResolver<T> resolver) {
                        tPromise.then(new Consumer<T>() {
                            @Override
                            public void apply(T t) {
                                resolver.result(t);
                            }
                        });
                        tPromise.failure(new Consumer<Exception>() {
                            @Override
                            public void apply(Exception e) {
                                resolver.result(null);
                            }
                        });
                        tPromise.done(resolver.getDispatcher());
                    }
                });
            }
        });
    }

    public PromisesArray<T> filterNull() {
        return filter(Predicates.NOT_NULL);
    }

    private <R> PromisesArray<R> mapSourcePromises(final Function<Promise<T>, Promise<R>> fun) {

        return new PromisesArray<R>(new PromiseFunc<Promise<R>[]>() {
            @Override
            public void exec(final PromiseResolver<Promise<R>[]> executor) {

                //
                // Handling source results
                //

                promises.then(new Consumer<Promise<T>[]>() {
                    @Override
                    public void apply(final Promise<T>[] sourcePromises) {

                        //
                        // Building mapped promises
                        //

                        final Promise<R>[] mappedPromises = new Promise[sourcePromises.length];

                        for (int i = 0; i < mappedPromises.length; i++) {

                            mappedPromises[i] = fun.apply(sourcePromises[i]);
                        }

                        //
                        // Returning mapped promises
                        //

                        executor.result(mappedPromises);
                    }
                });

                //
                // Handling failure
                //

                promises.failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        executor.error(e);
                    }
                });

                //
                // Starting source promises
                //

                promises.done(executor.getDispatcher());
            }
        });
    }

    public PromisesArray<T> filter(final Predicate<T> predicate) {
        return flatMap(new Function<T, T[]>() {

            @Override
            public T[] apply(T t) {
                if (predicate.apply(t)) {
                    return (T[]) new Object[]{t};
                }
                return (T[]) new Object[0];
            }
        });
    }

    public PromisesArray<T> sort(final Comparator<T> comparator) {
        return flatMapAll(new Function<T[], T[]>() {
            @Override
            public T[] apply(T[] ts) {
                T[] res = (T[]) new Object[ts.length];
                System.arraycopy(ts, 0, res, 0, ts.length);
                Arrays.sort(res, comparator);
                return res;
            }
        });
    }

    public PromisesArray<T> first(final int count) {
        return flatMapAll(new Function<T[], T[]>() {
            @Override
            public T[] apply(T[] ts) {
                int len = Math.min(count, ts.length);
                T[] res = (T[]) new Object[len];
                System.arraycopy(ts, 0, res, 0, len);
                return res;
            }
        });
    }

    public Promise<T> first() {
        return first(1)
                .zip()
                .map(new Function<List<T>, T>() {
                    @Override
                    public T apply(List<T> src) {
                        if (src.size() == 0) {
                            throw new RuntimeException("Array is empty (first)");
                        }
                        return src.get(0);
                    }
                });
    }

    public Promise<T> random() {
        return flatMapAll(new Function<T[], T[]>() {
            @Override
            public T[] apply(T[] ts) {
                if (ts.length == 0) {
                    throw new RuntimeException("Array is empty");
                }
                return (T[]) new Object[]{ts[rundom.randomInt(ts.length)]};
            }
        }).first();
    }

    public <R> PromisesArray<R> flatMapAll(final Function<T[], R[]> fuc) {
        return new PromisesArray<R>(new Promise<Promise<R>[]>() {
            @Override
            void exec(final PromiseResolver resolver) {
                //
                // Handling source results
                //

                promises.then(new Consumer<Promise<T>[]>() {
                    @Override
                    public void apply(final Promise<T>[] sourcePromises) {
                        final Object[] res = new Object[sourcePromises.length];
                        final Boolean[] ended = new Boolean[sourcePromises.length];
                        for (int i = 0; i < sourcePromises.length; i++) {
                            final int finalI = i;
                            sourcePromises[i].then(new Consumer<T>() {
                                @Override
                                public void apply(T t) {
                                    res[finalI] = t;
                                    ended[finalI] = true;

                                    for (int i1 = 0; i1 < sourcePromises.length; i1++) {
                                        if (ended[i1] == null || !ended[i1]) {
                                            return;
                                        }
                                    }

                                    R[] resMap = fuc.apply((T[]) res);


                                    ArrayList<Promise<R>> resultList = new ArrayList<>();
                                    for (R r : resMap) {
                                        resultList.add(Promises.success(r));
                                    }

                                    resolver.result(resultList.toArray(new Promise[0]));
                                }
                            });
                            sourcePromises[i].failure(new Consumer<Exception>() {
                                @Override
                                public void apply(Exception e) {
                                    resolver.error(e);
                                }
                            });
                            sourcePromises[i].done(resolver.getDispatcher());
                        }
                        if (sourcePromises.length == 0) {
                            resolver.result(new Promise[0]);
                        }
                    }
                });

                //
                // Handling failure
                //

                promises.failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        resolver.error(e);
                    }
                });

                //
                // Starting source promises
                //

                promises.done(resolver.getDispatcher());
            }
        });
    }

    public <R> PromisesArray<R> flatMap(final Function<T, R[]> map) {
        return new PromisesArray<R>(new Promise<Promise<R>[]>() {
            @Override
            void exec(final PromiseResolver resolver) {
                //
                // Handling source results
                //

                promises.then(new Consumer<Promise<T>[]>() {
                    @Override
                    public void apply(final Promise<T>[] sourcePromises) {
                        final Object[][] res = new Object[sourcePromises.length][];
                        final Boolean[] ended = new Boolean[sourcePromises.length];

                        for (int i = 0; i < sourcePromises.length; i++) {
                            final int finalI = i;
                            sourcePromises[i].then(new Consumer<T>() {
                                @Override
                                public void apply(T t) {

                                    res[finalI] = map.apply(t);
                                    ended[finalI] = true;

                                    for (int i1 = 0; i1 < sourcePromises.length; i1++) {
                                        if (ended[i1] == null || !ended[i1]) {
                                            return;
                                        }
                                    }

                                    ArrayList<Promise<R>> resultList = new ArrayList<>();
                                    for (int i2 = 0; i2 < sourcePromises.length; i2++) {
                                        for (int j = 0; j < res[i2].length; j++) {
                                            resultList.add(Promises.success((R) res[i2][j]));
                                        }
                                    }

                                    resolver.result(resultList.toArray(new Promise[0]));
                                }
                            });
                            sourcePromises[i].failure(new Consumer<Exception>() {
                                @Override
                                public void apply(Exception e) {
                                    resolver.error(e);
                                }
                            });
                            sourcePromises[i].done(resolver.getDispatcher());
                        }

                        if (sourcePromises.length == 0) {
                            resolver.result(new Promise[0]);
                        }
                    }
                });

                //
                // Handling failure
                //

                promises.failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        resolver.error(e);
                    }
                });

                //
                // Starting source promises
                //

                promises.done(resolver.getDispatcher());
            }
        });
    }

    /**
     * Zip array to single promise
     *
     * @param fuc zipping function
     * @param <R> type of result
     * @return promise
     */
    public <R> Promise<R> zipPromise(final ListFunction<T, Promise<R>> fuc) {
        return new Promise<R>() {
            @Override
            void exec(final PromiseResolver<R> resolver) {
                promises.then(new Consumer<Promise<T>[]>() {
                    @Override
                    public void apply(final Promise<T>[] promises1) {
                        final ArrayList<T> res = new ArrayList<T>();
                        for (int i = 0; i < promises1.length; i++) {
                            res.add(null);
                        }
                        final Boolean[] ended = new Boolean[promises1.length];

                        for (int i = 0; i < promises1.length; i++) {
                            final int finalI = i;
                            promises1[i].then(new Consumer<T>() {
                                @Override
                                public void apply(T t) {
                                    res.set(finalI, t);
                                    ended[finalI] = true;

                                    for (int i1 = 0; i1 < promises1.length; i1++) {
                                        if (ended[i1] == null || !ended[i1]) {
                                            return;
                                        }
                                    }

                                    Promise<R> promise = fuc.apply(res);
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
                            promises1[i].failure(new Consumer<Exception>() {
                                @Override
                                public void apply(Exception e) {
                                    resolver.error(e);
                                }
                            });
                            promises1[i].done(resolver.getDispatcher());
                        }

                        if (promises1.length == 0) {
                            Promise<R> promise = fuc.apply(res);
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
                    }
                });
                promises.failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        resolver.error(e);
                    }
                });

                promises.done(resolver.getDispatcher());
            }
        };
    }

    /**
     * Zipping array of promises to single promise of array
     *
     * @return promise
     */
    public Promise<List<T>> zip() {
        return zipPromise(new ListFunction<T, Promise<List<T>>>() {
            @Override
            public Promise<List<T>> apply(List<T> t) {
                return Promises.success(t);
            }
        });
    }
}