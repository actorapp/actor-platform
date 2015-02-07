package com.droidkit.actors.tasks;

/**
 * Callback for Ask pattern
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public interface AskCallback<T> {
    public void onResult(T result);

    public void onError(Throwable throwable);
}
