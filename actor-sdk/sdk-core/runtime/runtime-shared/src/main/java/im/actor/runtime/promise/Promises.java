package im.actor.runtime.promise;

import im.actor.runtime.Log;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;

/**
 * Various methods for creating promises.
 */
public class Promises {

    /**
     * Always success promise
     *
     * @param val success value
     * @param <T> type of value
     * @return promise
     */
    public static <T> Promise<T> success(final T val) {
        return new Promise<T>() {
            @Override
            void exec(PromiseResolver resolver) {
                resolver.result(val);
            }
        };
    }

    /**
     * Always failed promise
     *
     * @param e   fail reason
     * @param <T> type of promise
     * @return promise
     */
    public static <T> Promise<T> failure(final Exception e) {
        return new Promise<T>() {
            @Override
            void exec(PromiseResolver<T> resolver) {
                resolver.error(e);
            }
        };
    }

    public static <T> Promise<T> log(final String TAG, final PromiseResolver<T> resolver, final PromiseFunc<T> func) {
        return new Promise<T>() {
            @Override
            void exec(PromiseResolver<T> resolver) {
                func.exec(resolver);
            }
        }.then(new Consumer<T>() {
            @Override
            public void apply(T t) {
                Log.d(TAG, "Result: " + t);
                resolver.result(t);
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.d(TAG, "Error: " + e);
                Log.e(TAG, e);
                e.printStackTrace();
                resolver.error(e);
            }
        });
    }

    /**
     * Combines two promises to one with different data types
     *
     * @param t1   first argument
     * @param t2   second argument
     * @param <T1> first argument type
     * @param <T2> second argument type
     * @return promise
     */
    public static <T1, T2> Promise<Tuple2<T1, T2>> tuple(Promise<T1> t1, Promise<T2> t2) {

        return PromisesArray.ofPromises(t1.cast(), t2.cast())
                .zip()
                .map(new Function<Object[], Tuple2<T1,T2>>() {
                    @Override
                    public Tuple2<T1, T2> apply(Object[] src) {
                        return new Tuple2<T1, T2>((T1) src[0], (T2) src[1]);
                    }
                });
    }

    /**
     * Combines tree promises to one with different data types
     *
     * @param t1   first argument
     * @param t2   second argument
     * @param t3   third argument
     * @param <T1> first argument type
     * @param <T2> second argument type
     * @param <T3> third argument type
     * @return promise
     */
    public static <T1, T2, T3> Promise<Tuple3<T1, T2, T3>> tuple(Promise<T1> t1, Promise<T2> t2, Promise<T3> t3) {
        return PromisesArray.ofPromises(t1.cast(), t2.cast(), t3.cast())
                .zip()
                .map(new Function<Object[], Tuple3<T1, T2, T3>>() {
                    @Override
                    public Tuple3<T1, T2, T3> apply(Object[] src) {
                        return new Tuple3<T1, T2, T3>((T1) src[0], (T2) src[1], (T3) src[2]);
                    }
                });
    }

    /**
     * Combines four promises to one with different data types
     *
     * @param t1   first argument
     * @param t2   second argument
     * @param t3   third argument
     * @param t4   fourth argument
     * @param <T1> first argument type
     * @param <T2> second argument type
     * @param <T3> third argument type
     * @param <T4> fourth argument type
     * @return promise
     */
    public static <T1, T2, T3, T4> Promise<Tuple4<T1, T2, T3, T4>> tuple(Promise<T1> t1,
                                                                         Promise<T2> t2,
                                                                         Promise<T3> t3,
                                                                         Promise<T4> t4) {

        return PromisesArray.ofPromises(t1.cast(), t2.cast(), t3.cast(), t4.cast())
                .zip()
                .map(new Function<Object[], Tuple4<T1,T2,T3,T4>>() {
                    @Override
                    public Tuple4<T1, T2, T3, T4> apply(Object[] src) {
                        return new Tuple4<T1, T2, T3, T4>((T1) src[0], (T2) src[1], (T3) src[2], (T4) src[3]);
                    }
                });
    }
}
