package im.actor.runtime.promise;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import im.actor.runtime.Log;
import im.actor.runtime.function.ArrayFunction;
import im.actor.runtime.function.Map;
import im.actor.runtime.function.Consumer;

public class Promises {

    public static <T> Promise<T> success(final T val) {
        return new Promise<T>() {
            @Override
            void exec(PromiseResolver resolver) {
                Log.d("Promises", "success:" + val);
                resolver.result(val);
            }
        };
    }

    public static <T> Promise<T> successNotNull(final T val) {
        if (val == null){
            throw new RuntimeException("Val can't be null");
        }
        return success(val);
    }

    public static <T1, T2> Promise<Tuple2<T1, T2>> tuple(Promise<T1> t1, Promise<T2> t2) {
        return new Promise<Tuple2<T1, T2>>() {
            @Override
            void exec(PromiseResolver resolver) {
                final Object[] res = new Object[2];
                final boolean[] ended = new boolean[2];

                t1.then(t11 -> {
                    ended[0] = true;
                    res[0] = t11;

                    if (ended[0] && ended[1]) {
                        resolver.result(new Tuple2<>((T1) res[0], (T2) res[1]));
                    }
                });
                t1.failure(e -> resolver.error(e));

                t2.then(t11 -> {
                    ended[1] = true;
                    res[1] = t11;

                    if (ended[0] && ended[1]) {
                        resolver.result(new Tuple2<>((T1) res[0], (T2) res[1]));
                    }
                });
                t2.failure(e -> resolver.error(e));
                t1.done(resolver.getDispatcher());
                t2.done(resolver.getDispatcher());
            }
        };
    }

    public static <T1, T2, T3> Promise<Tuple3<T1, T2, T3>> tuple(Promise<T1> t1, Promise<T2> t2, Promise<T3> t3) {
        return new Promise<>(executor -> {
            final Object[] res = new Object[3];
            final boolean[] ended = new boolean[3];

            t1.then(t11 -> {
                ended[0] = true;
                res[0] = t11;

                if (ended[0] && ended[1] && ended[2]) {
                    executor.result(new Tuple3<>((T1) res[0], (T2) res[1], (T3) res[2]));
                }
            });
            t1.failure(e -> executor.error(e));
            t1.done(executor.getDispatcher());

            t2.then(t11 -> {
                ended[1] = true;
                res[1] = t11;

                if (ended[0] && ended[1] && ended[2]) {
                    executor.result(new Tuple3<>((T1) res[0], (T2) res[1], (T3) res[2]));
                }
            });
            t2.failure(e -> executor.error(e));
            t2.done(executor.getDispatcher());

            t3.then(t11 -> {
                ended[2] = true;
                res[2] = t11;

                if (ended[0] && ended[1] && ended[2]) {
                    executor.result(new Tuple3<>((T1) res[0], (T2) res[1], (T3) res[2]));
                }
            });
            t3.failure(e -> executor.error(e));
            t3.done(executor.getDispatcher());
        });
    }

    public static <T1, T2, T3, T4> Promise<Tuple4<T1, T2, T3, T4>> tuple(Promise<T1> t1,
                                                                         Promise<T2> t2,
                                                                         Promise<T3> t3,
                                                                         Promise<T4> t4) {
        return new Promise<>(executor -> {
            final Object[] res = new Object[4];
            final boolean[] ended = new boolean[4];

            t1.then(t11 -> {
                ended[0] = true;
                res[0] = t11;

                if (ended[0] && ended[1] && ended[2] && ended[3]) {
                    executor.result(new Tuple4<>((T1) res[0], (T2) res[1], (T3) res[2],
                            (T4) res[3]));
                }
            });
            t1.failure(e -> executor.error(e));
            t1.done(executor.getDispatcher());

            t2.then(t11 -> {
                ended[1] = true;
                res[1] = t11;

                if (ended[0] && ended[1] && ended[2] && ended[3]) {
                    executor.result(new Tuple4<>((T1) res[0], (T2) res[1], (T3) res[2],
                            (T4) res[3]));
                }
            });
            t2.failure(e -> executor.error(e));
            t2.done(executor.getDispatcher());

            t3.then(t11 -> {
                ended[2] = true;
                res[2] = t11;

                if (ended[0] && ended[1] && ended[2] && ended[3]) {
                    executor.result(new Tuple4<>((T1) res[0], (T2) res[1], (T3) res[2],
                            (T4) res[3]));
                }
            });
            t3.failure(e -> executor.error(e));
            t3.done(executor.getDispatcher());


            t4.then(t11 -> {
                ended[3] = true;
                res[3] = t11;

                if (ended[0] && ended[1] && ended[2] && ended[3]) {
                    executor.result(new Tuple4<>((T1) res[0], (T2) res[1], (T3) res[2],
                            (T4) res[3]));
                }
            });
            t4.failure(e -> executor.error(e));
            t4.done(executor.getDispatcher());
        });
    }

//    /**
//     * Zip promise of array to single object
//     *
//     * @param zip      zip method
//     * @param promises promises to zip
//     * @param <T>      array element types
//     * @param <R>      result element types
//     * @return updated promise
//     */
//    public static <T, R> Promise<R> zip(final Promise<T[]> promises, final ArrayFunction<T, R> zip) {
//        return new Promise<R>() {
//            @Override
//            protected void exec(@NotNull final PromiseResolver<R> executor) {
//                promises.complete(new PromiseCallback<T[]>() {
//                    @Override
//                    public void onResult(T[] ts) {
//                        R res;
//                        try {
//                            res = zip.apply(ts);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            executor.error(e);
//                            return;
//                        }
//                        executor.result(res);
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        executor.error(e);
//                    }
//                });
//                promises.done(promises.getDispatchActor());
//            }
//        };
//    }
//
//    /**
//     * Combining sequence of promises to one single promise
//     *
//     * @param promises source promises
//     * @param <T>      type of arguments
//     * @return result promise
//     */
//    @SafeVarargs
//    public static <T> Promise<T[]> sequence(final Promise<T>... promises) {
//        if (promises.length == 0) {
//            throw new RuntimeException("Promises array must not be empty");
//        }
//        return new Promise<T[]>() {
//            @Override
//            protected void exec(@NotNull final PromiseResolver<T[]> executor) {
//                final T[] res = (T[]) new Object[promises.length];
//                final boolean[] isSet = new boolean[promises.length];
//                final Promise self = this;
//                for (int i = 0; i < res.length; i++) {
//                    final int finalI = i;
//                    promises[i].then(new Consumer<T>() {
//                        @Override
//                        public void apply(T t) {
//                            if (self.isFinished()) {
//                                return;
//                            }
//
//                            res[finalI] = t;
//                            isSet[finalI] = true;
//                            for (int i = 0; i < promises.length; i++) {
//                                if (!isSet[i]) {
//                                    return;
//                                }
//                            }
//
//                            executor.result(res);
//                        }
//                    }).failure(new Consumer<Exception>() {
//                        @Override
//                        public void apply(Exception e) {
//                            if (self.isFinished()) {
//                                return;
//                            }
//
//                            executor.error(e);
//                        }
//                    });
//                }
//                for (Promise<T> p : promises) {
//                    p.done(self.getDispatchActor());
//                }
//            }
//        };
//    }
//
//    public static <T, C> Promise<T>[] map(Collection<C> items, Map<C, Promise<T>> map) {
//        ArrayList<Promise<T>> res = new ArrayList<Promise<T>>();
//        for (C c : items) {
//            res.add(map.map(c));
//        }
//        return res.toArray(new Promise[0]);
//    }
//
//    public static <T, C> Promise<T>[] map(C[] items, Map<C, Promise<T>> map) {
//        ArrayList<Promise<T>> res = new ArrayList<Promise<T>>();
//        for (C c : items) {
//            res.add(map.map(c));
//        }
//        return res.toArray(new Promise[0]);
//    }
}
