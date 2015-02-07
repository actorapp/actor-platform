package com.droidkit.actors.typed;

import com.droidkit.actors.concurrency.Future;

/**
 * Future for typed methods
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
 */
public class TypedFuture<T> extends Future<T> {
    public void doComplete(T res) {
        onCompleted(res);
    }

    public void doError(Throwable t) {
        onError(t);
    }
}
