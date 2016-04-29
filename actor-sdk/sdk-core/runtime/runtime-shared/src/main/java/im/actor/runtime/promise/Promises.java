package im.actor.runtime.promise;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.List;

import im.actor.runtime.Log;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Supplier;
import im.actor.runtime.function.Tuple2;
import im.actor.runtime.function.Tuple3;
import im.actor.runtime.function.Tuple4;

/**
 * Various methods for creating promises.
 */
public class Promises {

    @ObjectiveCName("logWithTag:withResolver:withFunc:")
    public static <T> Promise<T> log(final String TAG, final PromiseResolver<T> resolver, final PromiseFunc<T> func) {
        return new Promise<T>(r -> func.exec(r)).then(t -> {
            Log.d(TAG, "Result: " + t);
            resolver.result(t);
        }).failure(e -> {
            Log.d(TAG, "Error: " + e);
            Log.e(TAG, e);
            e.printStackTrace();
            resolver.error(e);
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
    @ObjectiveCName("tupleWithT1:withT2:")
    public static <T1, T2> Promise<Tuple2<T1, T2>> tuple(Promise<T1> t1, Promise<T2> t2) {

        return PromisesArray.ofPromises((Promise<Object>) t1, (Promise<Object>) t2)
                .zip()
                .map(src -> new Tuple2<>((T1) src.get(0), (T2) src.get(1)));
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
    @ObjectiveCName("tupleWithT1:withT2:withT3:")
    public static <T1, T2, T3> Promise<Tuple3<T1, T2, T3>> tuple(Promise<T1> t1, Promise<T2> t2, Promise<T3> t3) {
        return PromisesArray.ofPromises((Promise<Object>) t1, (Promise<Object>) t2, (Promise<Object>) t3)
                .zip()
                .map(src -> new Tuple3<>((T1) src.get(0), (T2) src.get(1), (T3) src.get(3)));
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
    @ObjectiveCName("tupleWithT1:withT2:withT3:withT4:")
    public static <T1, T2, T3, T4> Promise<Tuple4<T1, T2, T3, T4>> tuple(Promise<T1> t1,
                                                                         Promise<T2> t2,
                                                                         Promise<T3> t3,
                                                                         Promise<T4> t4) {

        return PromisesArray.ofPromises((Promise<Object>) t1, (Promise<Object>) t2, (Promise<Object>) t3, (Promise<Object>) t4)
                .zip()
                .map(src -> new Tuple4<>((T1) src.get(0), (T2) src.get(1), (T3) src.get(2), (T4) src.get(3)));
    }

    /**
     * Execute promises step by step
     *
     * @param queue queue of promises
     * @param <T>   type of promises
     * @return promise
     */
    public static <T> Promise traverse(List<Supplier<Promise<T>>> queue) {

        if (queue.size() == 0) {
            return Promise.success(null);
        }

        return queue.remove(0).get()
                .flatMap(v -> traverse(queue));
    }
}