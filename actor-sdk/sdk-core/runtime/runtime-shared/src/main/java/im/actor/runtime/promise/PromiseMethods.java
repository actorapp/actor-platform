package im.actor.runtime.promise;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.Log;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.ConsumerDouble;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Supplier;

public interface PromiseMethods<T, V extends PromiseMethods<T, V>> {

    /**
     * Handling successful result
     *
     * @param then supplier for result
     * @return this
     */
    @ObjectiveCName("then:")
    V then(final Consumer<T> then);

    /**
     * Handling failure
     *
     * @param failure supplier for exception
     * @return this
     */
    @ObjectiveCName("failure:")
    V failure(final Consumer<Exception> failure);


    default V complete(final ConsumerDouble<T, Exception> completeHandler) {
        then(t -> completeHandler.apply(t, null));
        failure(e -> completeHandler.apply(null, e));
        return (V) this;
    }


    /**
     * Pipe result to resolver
     *
     * @param resolver destination resolver
     * @return this
     */
    @ObjectiveCName("pipeTo:")
    default PromiseMethods<T, V> pipeTo(final PromiseResolver<T> resolver) {
        then(resolver::result);
        failure(resolver::error);
        return this;
    }

    @ObjectiveCName("log:")
    default PromiseMethods<T, V> log(final String TAG) {
        then(t -> Log.d(TAG, "Result: " + t));
        failure(e -> Log.w(TAG, "Error: " + e));
        return this;
    }

    @ObjectiveCName("mapIfNull:")
    default Promise<T> mapIfNull(final Supplier<T> producer) {
        final PromiseMethods<T, V> self = this;
        return new Promise<T>(resolver -> {
            self.then(t -> {
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
            });
            self.failure(resolver::error);
        });
    }

    @ObjectiveCName("mapIfNullPromise:")
    default Promise<T> mapIfNullPromise(final Supplier<Promise<T>> producer) {
        final PromiseMethods<T, V> self = this;
        return new Promise<T>(resolver -> {
            self.then(t -> {
                if (t == null) {
                    Promise<T> promise;
                    try {
                        promise = producer.get();
                    } catch (Exception e) {
                        resolver.error(e);
                        return;
                    }
                    promise.then(resolver::result);
                    promise.failure(resolver::error);
                } else {
                    resolver.result(t);
                }
            });
            self.failure(resolver::error);
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
        final PromiseMethods<T, V> self = this;
        return new Promise<>((PromiseFunc<R>) resolver -> {
            self.then(t -> {
                R r;
                try {
                    r = res.apply(t);
                } catch (Exception e) {
                    e.printStackTrace();
                    resolver.tryError(e);
                    return;
                }
                resolver.tryResult(r);
            });
            self.failure(resolver::error);
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
        final PromiseMethods<T, V> self = this;
        return new Promise<>((PromiseFunc<R>) resolver -> {
            self.then(t -> {
                Promise<R> promise;
                try {
                    promise = res.apply(t);
                } catch (Exception e) {
                    e.printStackTrace();
                    resolver.tryError(e);
                    return;
                }

                promise.then(resolver::result);
                promise.failure(resolver::error);
            });
            self.failure(resolver::tryError);
        });
    }

    default <R> Promise<T> mapPromiseSelf(final Function<T, Promise<R>> res) {
        return mapPromise(t -> res.apply(t).map((Function<R, T>) r -> t));
    }

    @ObjectiveCName("fallback:")
    default Promise<T> fallback(final Function<Exception, Promise<T>> catchThen) {
        final PromiseMethods<T, V> self = this;
        return new Promise<T>(resolver -> {
            self.then(resolver::result);
            self.failure(e -> {
                Promise<T> res = catchThen.apply(e);
                res.then(resolver::result);
                res.failure(resolver::error);
            });
        });
    }

    @ObjectiveCName("afterVoid:")
    default <R> Promise<R> afterVoid(final Supplier<Promise<R>> promiseSupplier) {
        final PromiseMethods<T, V> self = this;
        return new Promise<R>(resolver -> {
            self.then(t -> {
                Promise<R> promise = promiseSupplier.get();
                promise.then(resolver::result);
                promise.failure(resolver::error);
            });
            self.failure(resolver::error);
        });
    }

}
