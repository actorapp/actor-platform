package com.droidkit.actors.typed;

import com.droidkit.actors.concurrency.Future;

/**
 * Created by ex3ndr on 14.09.14.
 */
class ResultFuture<T> extends Future<T> {
    ResultFuture(T res) {
        onCompleted(res);
    }
}
