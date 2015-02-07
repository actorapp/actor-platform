package com.droidkit.actors.typed;

import com.droidkit.actors.concurrency.Future;

/**
 * Created by ex3ndr on 14.09.14.
 */
public class ClientFuture<T> extends Future<T> {
    public ClientFuture() {

    }

    void doComplete(T res) {
        onCompleted(res);
    }

    void doError(Throwable t) {
        onError(t);
    }
}
