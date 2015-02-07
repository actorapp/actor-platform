package com.droidkit.actors.concurrency;

import java.util.LinkedList;

/**
 * <p>Actors Future</p>
 * For now it is expected to use only inside actor model
 */
public abstract class Future<T> {

    private LinkedList<FutureCallback<T>> callbacks = new LinkedList<FutureCallback<T>>();

    private boolean isCompleted = false;
    private boolean isCanceled = false;
    private boolean isError = false;
    private T result = null;
    private Throwable error = null;

    public synchronized boolean isCompleted() {
        return isCompleted;
    }

    public synchronized boolean isError() {
        return isError;
    }

    public synchronized boolean isCanceled() {
        return isCanceled;
    }

    public synchronized Throwable error() {
        return error;
    }

    public synchronized T get() {
        return result;
    }

    public synchronized boolean addListener(FutureCallback<T> callback) {
        callbacks.add(callback);
        if (isCompleted) {
            if (isCanceled) {
                return true;
            }
            if (isError) {
                callback.onError(error);
            } else {
                callback.onResult(result);
            }
        }
        return isCompleted;
    }

    public synchronized void removeListener(FutureCallback<T> callback) {
        callbacks.remove(callback);
    }

    protected synchronized void onCancel() {
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        isError = false;
        isCanceled = true;
    }

    protected synchronized void onCompleted(T res) {
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        isCanceled = false;
        isError = false;
        error = null;
        result = res;

        for (FutureCallback<T> callback : callbacks) {
            callback.onResult(res);
        }
    }

    protected synchronized void onError(Throwable throwable) {
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        isCanceled = false;
        isError = true;
        error = throwable;
        result = null;

        for (FutureCallback callback : callbacks) {
            callback.onError(throwable);
        }
    }

    protected synchronized void onTimeout() {
        onError(new FutureTimeoutException());
    }
}
