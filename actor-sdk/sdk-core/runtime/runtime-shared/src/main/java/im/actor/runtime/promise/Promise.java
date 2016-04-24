package im.actor.runtime.promise;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.runtime.function.Consumer;
import im.actor.runtime.threading.SimpleDispatcher;
import im.actor.runtime.threading.ThreadDispatcher;

/**
 * Promise support implementations. It is much more like js promises than traditional
 * java/scala versions.
 *
 * @param <T> type of result
 */
public class Promise<T> implements PromiseMethods<T, Promise<T>> {

    /**
     * Success promise. Have result immediately.
     *
     * @param val success value
     * @param <T> type of value
     * @return promise
     */
    @ObjectiveCName("success:")
    public static <T> Promise<T> success(T val) {
        return new Promise<>(val);
    }

    /**
     * Failed promise. Have result immediately.
     *
     * @param e   fail reason
     * @param <T> type of promise
     * @return promise
     */
    @ObjectiveCName("failure:")
    public static <T> Promise<T> failure(Exception e) {
        return new Promise<>(e);
    }

    //
    // Dispatching parameters
    //

    private final ArrayList<PromiseCallback<T>> callbacks = new ArrayList<>();
    private final SimpleDispatcher dispatcher;

    //
    // State of Promise
    //

    private volatile T result;
    private volatile Exception exception;
    private volatile boolean isFinished;

    /**
     * Default constructor of promise
     *
     * @param executor Executor
     */
    @ObjectiveCName("initWithExecutor:")
    public Promise(PromiseFunc<T> executor) {
        this.dispatcher = ThreadDispatcher.peekDispatcher();
        executor.exec(new PromiseResolver<>(this));
    }

    /**
     * Successful constructor of promise
     *
     * @param value value
     */
    @ObjectiveCName("initWithValue:")
    private Promise(T value) {
        this.dispatcher = ThreadDispatcher.peekDispatcher();
        this.result = value;
        this.exception = null;
        this.isFinished = true;
    }

    /**
     * Exception constructor of promise
     *
     * @param e exception
     */
    @ObjectiveCName("initWithException:")
    private Promise(Exception e) {
        this.dispatcher = ThreadDispatcher.peekDispatcher();
        this.result = null;
        this.exception = e;
        this.isFinished = true;
    }


    //
    // Receiving Results
    //

    /**
     * Handling successful result
     *
     * @param then supplier for result
     * @return this
     */
    @ObjectiveCName("then:")
    public synchronized Promise<T> then(final Consumer<T> then) {
        if (isFinished) {
            if (exception == null) {
                dispatcher.dispatch(() -> then.apply(result));
            }
        } else {
            callbacks.add(new PromiseCallback<T>() {
                @Override
                public void onResult(T t) {
                    then.apply(t);
                }

                @Override
                public void onError(Exception e) {
                    // Do nothing
                }
            });
        }
        return this;
    }

    /**
     * Handling failure
     *
     * @param failure supplier for exception
     * @return this
     */
    @ObjectiveCName("failure:")
    public synchronized Promise<T> failure(final Consumer<Exception> failure) {
        if (isFinished) {
            if (exception != null) {
                dispatcher.dispatch(() -> failure.apply(exception));
            }
        } else {

            callbacks.add(new PromiseCallback<T>() {
                @Override
                public void onResult(T t) {
                    // Do nothing
                }

                @Override
                public void onError(Exception e) {
                    failure.apply(e);
                }
            });
        }
        return this;
    }


    //
    // Delivering Results
    //

    /**
     * Called when promise ended with error
     *
     * @param e error
     */
    synchronized void error(@NotNull Exception e) {
        if (isFinished) {
            throw new RuntimeException("Promise already completed!");
        }
        if (e == null) {
            throw new RuntimeException("Error can't be null");
        }
        isFinished = true;
        exception = e;
        deliverResult();
    }

    /**
     * Trying complete promise with error
     *
     * @param e error
     */
    synchronized void tryError(@NotNull Exception e) {
        if (isFinished) {
            return;
        }
        error(e);
    }

    /**
     * Called when result is ready
     *
     * @param res result
     */
    synchronized void result(@Nullable T res) {
        if (isFinished) {
            throw new RuntimeException("Promise " + this + " already completed!");
        }
        isFinished = true;
        result = res;
        deliverResult();
    }

    /**
     * Trying complete promise with result
     *
     * @param res result
     */
    synchronized void tryResult(@Nullable T res) {
        if (isFinished) {
            return;
        }
        result(res);
    }

    /**
     * Delivering result
     */
    private void deliverResult() {
        if (callbacks.size() > 0) {
            dispatcher.dispatch(() -> {
                if (exception != null) {
                    for (PromiseCallback<T> callback : callbacks) {
                        try {
                            callback.onError(exception);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    for (PromiseCallback<T> callback : callbacks) {
                        try {
                            callback.onResult(result);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                callbacks.clear();
            });
        }
    }

    /**
     * Callback for retrieving result of promise
     *
     * @param <T> type of successful result
     */
    interface PromiseCallback<T> {
        @ObjectiveCName("onResult:")
        void onResult(T t);

        @ObjectiveCName("onError:")
        void onError(Exception e);
    }
}