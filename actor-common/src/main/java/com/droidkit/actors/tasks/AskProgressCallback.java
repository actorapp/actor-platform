package com.droidkit.actors.tasks;

/**
 * Callback for Ask pattern
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public interface AskProgressCallback<T, V> extends AskCallback<T> {
    public void onProgress(V v);
}
