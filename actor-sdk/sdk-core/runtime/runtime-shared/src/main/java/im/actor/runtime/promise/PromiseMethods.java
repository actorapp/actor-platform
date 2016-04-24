package im.actor.runtime.promise;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.Log;
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


    /**
     * Called after success or failure
     *
     * @param afterHandler after handler
     * @return this
     */
    default Promise<T> after(final ConsumerDouble<T, Exception> afterHandler) {
        then(t -> afterHandler.apply(t, null));
        failure(e -> afterHandler.apply(null, e));
        return (Promise<T>) this;
    }

    /**
     * Pipe result to resolver
     *
     * @param resolver destination resolver
     * @return this
     */
    @ObjectiveCName("pipeTo:")
    default Promise<T> pipeTo(final PromiseResolver<T> resolver) {
        then(resolver::result);
        failure(resolver::error);
        return (Promise<T>) this;
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
        final PromiseMethods<T> self = this;
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
        final PromiseMethods<T> self = this;
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

    @ObjectiveCName("fallback:")
    default Promise<T> fallback(final Function<Exception, Promise<T>> catchThen) {
        final PromiseMethods<T> self = this;
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
        final PromiseMethods<T> self = this;
        return new Promise<R>(resolver -> {
            self.then(t -> {
                Promise<R> promise = promiseSupplier.get();
                promise.then(resolver::result);
                promise.failure(resolver::error);
            });
            self.failure(resolver::error);
        });
    }


    @ObjectiveCName("mapIfNull:")
    default Promise<T> mapIfNull(final Supplier<T> producer) {
        final PromiseMethods<T> self = this;
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
        final PromiseMethods<T> self = this;
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

    @ObjectiveCName("log:")
    default Promise<T> log(final String TAG) {
        then(t -> Log.d(TAG, "Result: " + t));
        failure(e -> Log.w(TAG, "Error: " + e));
        return (Promise<T>) this;
    }
}
