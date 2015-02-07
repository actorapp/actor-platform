package com.droidkit.actors.concurrency;

/**
 * Created by ex3ndr on 14.09.14.
 */
public interface FutureCallback<T> {
    public void onResult(T result);

    public void onError(Throwable throwable);
}
