package im.actor.runtime.promise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import im.actor.runtime.Log;
import im.actor.runtime.function.ArrayFunction;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;

public class PromisesArray<T> {

    private Promise<Promise<T>[]> promises;

    private PromisesArray(Promise<Promise<T>[]> promises) {
        this.promises = promises;
    }

    private PromisesArray(PromiseFunc<Promise<T>[]> executor) {
        this(new Promise<>(executor));
    }

    public <R> PromisesArray<R> map(Function<T, Promise<R>> fun) {
        return new PromisesArray<>(executor -> {
            promises.then(sourcePromises -> {
                Log.d("PromisesArray", "1");
                // Starting source promises
                for (int i = 0; i < sourcePromises.length; i++) {
                    sourcePromises[i].done(executor.getDispatcher());
                }
                // Building mapped promises
                final Promise<R>[] mappedPromises = new Promise[sourcePromises.length];
                for (int i = 0; i < mappedPromises.length; i++) {
                    final int finalI = i;
                    final Function<T, Promise<R>> fun2 = fun;
                    mappedPromises[finalI] = new Promise<R>() {
                        @Override
                        void exec(PromiseResolver<R> resolver) {
                            sourcePromises[finalI].then(new Consumer<T>() {
                                @Override
                                public void apply(T t) {
                                    fun2.apply(t)
                                            .then(new Consumer<R>() {
                                                @Override
                                                public void apply(R r) {
                                                    resolver.result(r);
                                                }
                                            })
                                            .failure(new Consumer<Exception>() {
                                                @Override
                                                public void apply(Exception e) {
                                                    resolver.error(e);
                                                }
                                            }).done(resolver.getDispatcher());
                                    ;
                                }
                            });
                            sourcePromises[finalI].failure(e -> {
                                resolver.error(e);
                            });
                        }
                    };
                }
                executor.result(mappedPromises);
                Log.d("PromisesArray", "5");
            });
            Log.d("PromisesArray", "map:executor:1");
            promises.failure(e -> executor.error(e));
            Log.d("PromisesArray", "map:executor:2");
            promises.done(executor.getDispatcher());
            Log.d("PromisesArray", "map:executor:3");
        });
    }

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
//                        promises1[i].then(t -> {
//
//                            res[finalI] = t;
//                            ended[finalI] = true;
//
//                            for (int i1 = 0; i1 < promises1.length; i1++) {
//                                if (ended[i1] == null || !ended[i1]) {
//                                    return;
//                                }
//                            }
//
////                            Promise<R> promise = fuc.apply((T[]) res);
////                            promise.then(r -> resolver.result(r));
////                            promise.failure(e -> resolver.error(e));
////                            promise.done(resolver.getDispatcher());
//                        });
//                        promises1[i].failure(e -> resolver.error(e));
//                        promises1[i].done(resolver.getDispatcher());
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
//                promises.failure(e -> resolver.error(e));
//                promises.done(resolver.getDispatcher());
            }
        };
    }

    public Promise<T[]> zip() {
        return zipPromise(new ArrayFunction<T, Promise<T[]>>() {
            @Override
            public Promise<T[]> apply(T[] t) {
                return Promises.success(t);
            }
        });
    }

    public static <T> PromisesArray<T> of(List<T> list) {
        ArrayList<Promise<T>> res = new ArrayList<>();
        for (T t : list) {
            res.add(Promises.success(t));
        }
        Promise[] promises = (Promise[]) res.toArray();
        return new PromisesArray<>(executor -> executor.result(promises));
    }

    @SafeVarargs
    public static <T> PromisesArray<T> of(T... items) {
        ArrayList<Promise<T>> res = new ArrayList<>();
        for (T t : items) {
            res.add(Promises.success(t));
        }
        Promise[] promises = (Promise[]) res.toArray();
        return new PromisesArray<>(executor -> executor.result(promises));
    }

    @SafeVarargs
    public static <T> PromisesArray<T> ofPromises(Promise<T>... items) {
        ArrayList<Promise<T>> res = new ArrayList<>();
        Collections.addAll(res, items);
        Promise[] promises = (Promise[]) res.toArray();
        return new PromisesArray<>(executor -> executor.result(promises));
    }
}