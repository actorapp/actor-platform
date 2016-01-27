package im.actor.runtime.promise;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import im.actor.runtime.function.ArrayFunction;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;

/**
 * Array of Promises. Allows you to invoke map, mapPromise and other useful methods
 * for manipulating data.
 *
 * @param <T> type of array
 */
public class PromisesArray<T> {

    /**
     * Create PromisesArray from collection
     *
     * @param collection Source collection
     * @param <T>        type of array
     * @return array
     */
    public static <T> PromisesArray<T> of(Collection<T> collection) {
        ArrayList<Promise<T>> res = new ArrayList<>();
        for (T t : collection) {
            res.add(Promises.success(t));
        }
        Promise[] promises = (Promise[]) res.toArray();
        return new PromisesArray<>(executor -> executor.result(promises));
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
        Promise[] promises = (Promise[]) res.toArray();
        return new PromisesArray<>(executor -> executor.result(promises));
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
        Promise[] promises = (Promise[]) res.toArray();
        return new PromisesArray<>(executor -> executor.result(promises));
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
    public <R> PromisesArray<R> map(Function<T, Promise<R>> fun) {
        return new PromisesArray<>(executor -> {

            //
            // Handling source results
            //

            promises.then(sourcePromises -> {

                //
                // Building mapped promises
                //

                final Promise<R>[] mappedPromises = new Promise[sourcePromises.length];

                for (int i = 0; i < mappedPromises.length; i++) {
                    final int finalI = i;
                    final Function<T, Promise<R>> fun2 = fun;

                    mappedPromises[finalI] = new Promise<R>() {
                        @Override
                        void exec(PromiseResolver<R> resolver) {

                            //
                            // Handling results from source PromisesArray
                            //

                            sourcePromises[finalI].then(new Consumer<T>() {
                                @Override
                                public void apply(T t) {

                                    //
                                    // Mapping value to promise
                                    //
                                    Promise<R> mapped = fun2.apply(t);

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

                            sourcePromises[finalI].failure(new Consumer<Exception>() {
                                @Override
                                public void apply(Exception e) {
                                    resolver.error(e);
                                }
                            });

                            //
                            // Starting source promise
                            //

                            sourcePromises[finalI].done(resolver.getDispatcher());
                        }
                    };
                }

                //
                // Returning mapped promises
                //

                executor.result(mappedPromises);
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
        });
    }

    /**
     * Zip array to single promise
     *
     * @param fuc zipping function
     * @param <R> type of result
     * @return promise
     */
    public <R> Promise<R> zipPromise(ArrayFunction<T, Promise<R>> fuc) {
        return new Promise<R>() {
            @Override
            void exec(final PromiseResolver resolver) {
                promises.then(new Consumer<Promise<T>[]>() {
                    @Override
                    public void apply(Promise<T>[] promises1) {
                        final Object[] res = new Object[promises1.length];
                        final Boolean[] ended = new Boolean[promises1.length];

                        for (int i = 0; i < promises1.length; i++) {
                            final int finalI = i;
                            promises1[i].then(new Consumer<T>() {
                                @Override
                                public void apply(T t) {
                                    res[finalI] = t;
                                    ended[finalI] = true;

                                    for (int i1 = 0; i1 < promises1.length; i1++) {
                                        if (ended[i1] == null || !ended[i1]) {
                                            return;
                                        }
                                    }

                                    Promise<R> promise = fuc.apply((T[]) res);
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
    public Promise<T[]> zip() {
        return zipPromise(new ArrayFunction<T, Promise<T[]>>() {
            @Override
            public Promise<T[]> apply(T[] t) {
                return Promises.success(t);
            }
        });
    }
}