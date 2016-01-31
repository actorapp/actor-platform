package im.actor.runtime.actors.future;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import im.actor.runtime.actors.messages.Void;

public class Future<T> {

    private final ArrayList<FutureCallback<T>> callbacks = new ArrayList<FutureCallback<T>>();

    private volatile T result;
    private volatile Exception exception;
    private volatile boolean isFinished;

    public void onResult(@NotNull T result) {
        if (isFinished) {
            throw new RuntimeException("Already finished!");
        }
        this.isFinished = true;
        this.result = result;
        deliverResult();
    }

    public void onError(@NotNull Exception e) {
        if (isFinished) {
            throw new RuntimeException("Already finished!");
        }
        this.isFinished = true;
        this.exception = e;
        deliverResult();
    }

    private synchronized void deliverResult() {
        if (exception != null) {
            for (FutureCallback<T> callback : callbacks) {
                callback.onError(exception);
            }
        } else {
            for (FutureCallback<T> callback : callbacks) {
                callback.onResult(result);
            }
        }
    }

    public synchronized void subscribe(FutureCallback<T> callback) {
        if (isFinished) {
            if (exception != null) {
                callback.onError(exception);
            } else {
                callback.onResult(result);
            }
        } else {
            if (!callbacks.contains(callback)) {
                callbacks.add(callback);
            }
        }
    }

    public synchronized void unsubscribe(FutureCallback<T> callback) {
        callbacks.remove(callback);
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isSuccess() {
        return isFinished && exception == null;
    }

    public T getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }
}