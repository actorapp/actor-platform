package im.actor.runtime.promise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import im.actor.runtime.Crypto;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.ListFunction;
import im.actor.runtime.function.Predicate;
import im.actor.runtime.function.Predicates;

/**
 * Array of Promises. Allows you to invoke map, flatMap and other useful methods
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
    @SuppressWarnings("unchecked")
    public static <T> PromisesArray<T> of(Collection<T> collection) {
        final ArrayList<Promise<T>> res = new ArrayList<>();
        for (T t : collection) {
            res.add(Promise.success(t));
        }
        final Promise[] promises = res.toArray(new Promise[res.size()]);
        return new PromisesArray<>((PromiseFunc<Promise<T>[]>) executor -> {
            executor.result(promises);
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
            res.add(Promise.success(t));
        }
        final Promise[] promises = res.toArray(new Promise[res.size()]);
        return new PromisesArray<>((PromiseFunc<Promise<T>[]>) executor -> {
            executor.result(promises);
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
        return new PromisesArray<>((PromiseFunc<Promise<T>[]>) executor -> {
            executor.result(promises);
        });
    }

    public static <T> PromisesArray<T> ofPromises(Collection<Promise<T>> items) {
        ArrayList<Promise<T>> res = new ArrayList<>(items);
        // Collections.addAll(res, items);
        final Promise[] promises = res.toArray(new Promise[res.size()]);
        return new PromisesArray<>((PromiseFunc<Promise<T>[]>) executor -> {
            executor.result(promises);
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
        return mapSourcePromises(srcPromise -> new Promise<R>(resolver -> {
            srcPromise.then(t -> {
                Promise<R> mapped = fun.apply(t);
                mapped.then(t2 -> resolver.result(t2));
                mapped.failure(e -> resolver.error(e));
            });
            srcPromise.failure(e -> resolver.error(e));
        }));
    }

    public <R> PromisesArray<R> mapOptional(final Function<T, Promise<R>> fun) {
        return map(fun)
                .ignoreFailed()
                .filterNull();
    }

    public PromisesArray<T> ignoreFailed() {
        return mapSourcePromises(tPromise -> new Promise<T>(resolver -> {
            tPromise.then(t -> resolver.result(t));
            tPromise.failure(e -> resolver.result(null));
        }));
    }

    public PromisesArray<T> filterNull() {
        return filter(Predicates.NOT_NULL);
    }

    private <R> PromisesArray<R> mapSourcePromises(final Function<Promise<T>, Promise<R>> fun) {

        return new PromisesArray<R>(executor -> {

            //
            // Handling source results
            //

            promises.then(sourcePromises -> {

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
            });

            //
            // Handling failure
            //

            promises.failure(e -> executor.error(e));
        });
    }

    public PromisesArray<T> filter(final Predicate<T> predicate) {
        return flatMap(t -> {
            if (predicate.apply(t)) {
                return (T[]) new Object[]{t};
            }
            return (T[]) new Object[0];
        });
    }

    public PromisesArray<T> sort(final Comparator<T> comparator) {
        return flatMapAll(ts -> {
            T[] res = (T[]) new Object[ts.length];
            System.arraycopy(ts, 0, res, 0, ts.length);
            Arrays.sort(res, comparator);
            return res;
        });
    }

    public PromisesArray<T> first(final int count) {
        return flatMapAll(ts -> {
            int len = Math.min(count, ts.length);
            T[] res = (T[]) new Object[len];
            System.arraycopy(ts, 0, res, 0, len);
            return res;
        });
    }

    public Promise<T> first() {
        return first(1)
                .zip()
                .map(src -> {
                    if (src.size() == 0) {
                        throw new RuntimeException("Array is empty (first)");
                    }
                    return src.get(0);
                });
    }

    public Promise<T> random() {
        return flatMapAll(ts -> {
            if (ts.length == 0) {
                throw new RuntimeException("Array is empty");
            }
            return (T[]) new Object[]{ts[Crypto.randomInt(ts.length)]};
        }).first();
    }

    public <R> PromisesArray<R> flatMapAll(final Function<T[], R[]> fuc) {
        return new PromisesArray<R>(new Promise<>((PromiseFunc<Promise<R>[]>) resolver -> {

            //
            // Handling source results
            //

            promises.then(sourcePromises -> {
                final Object[] res = new Object[sourcePromises.length];
                final Boolean[] ended = new Boolean[sourcePromises.length];
                for (int i = 0; i < sourcePromises.length; i++) {
                    final int finalI = i;
                    sourcePromises[i].then(t -> {
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
                            resultList.add(Promise.success(r));
                        }

                        resolver.result(resultList.toArray(new Promise[0]));
                    });
                    sourcePromises[i].failure(e -> resolver.error(e));
                }
                if (sourcePromises.length == 0) {
                    resolver.result(new Promise[0]);
                }
            });

            //
            // Handling failure
            //

            promises.failure(e -> resolver.error(e));
        }));
    }

    public <R> PromisesArray<R> flatMap(final Function<T, R[]> map) {
        return new PromisesArray<R>(new Promise<>((PromiseFunc<Promise<R>[]>) resolver -> {

            //
            // Handling source results
            //

            promises.then(sourcePromises -> {
                final Object[][] res = new Object[sourcePromises.length][];
                final Boolean[] ended = new Boolean[sourcePromises.length];

                for (int i = 0; i < sourcePromises.length; i++) {
                    final int finalI = i;
                    sourcePromises[i].then(t -> {

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
                                resultList.add(Promise.success((R) res[i2][j]));
                            }
                        }

                        resolver.result(resultList.toArray(new Promise[0]));
                    });
                    sourcePromises[i].failure(e -> resolver.error(e));
                }

                if (sourcePromises.length == 0) {
                    resolver.result(new Promise[0]);
                }
            });

            //
            // Handling failure
            //

            promises.failure(e -> resolver.error(e));
        }));
    }

    /**
     * Zip array to single promise
     *
     * @param fuc zipping function
     * @param <R> type of result
     * @return promise
     */
    public <R> Promise<R> zipPromise(final ListFunction<T, Promise<R>> fuc) {
        return new Promise<>(resolver -> {
            promises.then(promises1 -> {
                final ArrayList<T> res = new ArrayList<T>();
                for (int i = 0; i < promises1.length; i++) {
                    res.add(null);
                }
                final Boolean[] ended = new Boolean[promises1.length];

                for (int i = 0; i < promises1.length; i++) {
                    final int finalI = i;
                    promises1[i].then(t -> {
                        res.set(finalI, t);
                        ended[finalI] = true;

                        for (int i1 = 0; i1 < promises1.length; i1++) {
                            if (ended[i1] == null || !ended[i1]) {
                                return;
                            }
                        }

                        fuc.apply(res)
                                .pipeTo(resolver);
                    });
                    promises1[i].failure(e -> resolver.error(e));
                }

                if (promises1.length == 0) {
                    fuc.apply(res)
                            .pipeTo(resolver);
                }
            });
            promises.failure(e -> resolver.error(e));
        });
    }

    /**
     * Zipping array of promises to single promise of array
     *
     * @return promise
     */
    public Promise<List<T>> zip() {
        return zipPromise(t -> Promise.success(t));
    }
}