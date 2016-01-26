package im.actor.runtime.actors.promise;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.function.ArrayFunction;
import im.actor.runtime.function.Supplier;

public class Promises {

    /**
     * Zip promise of array to single object
     *
     * @param zip      zip method
     * @param promises promises to zip
     * @param <T>      array element types
     * @param <R>      result element types
     * @return updated promise
     */
    public static <T, R> Promise<R> zip(final Promise<T[]> promises, final ArrayFunction<T, R> zip) {
        return new Promise<R>() {
            @Override
            protected void exec(@NotNull final PromiseResolver<R> executor) {
                promises.complete(new PromiseCallback<T[]>() {
                    @Override
                    public void onResult(T[] ts) {
                        R res;
                        try {
                            res = zip.apply(ts);
                        } catch (Exception e) {
                            e.printStackTrace();
                            executor.error(e);
                            return;
                        }
                        executor.result(res);
                    }

                    @Override
                    public void onError(Exception e) {
                        executor.error(e);
                    }
                });
                promises.done();
            }
        };
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
            protected void exec(@NotNull final PromiseResolver<T[]> executor) {
                final T[] res = (T[]) new Object[promises.length];
                final boolean[] isSet = new boolean[promises.length];
                final Promise self = this;
                for (int i = 0; i < res.length; i++) {
                    final int finalI = i;
                    promises[i].then(new Supplier<T>() {
                        @Override
                        public void apply(T t) {
                            if (self.isFinished()) {
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
                            if (self.isFinished()) {
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
