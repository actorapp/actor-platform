package com.droidkit.actors.typed.messages;

/**
 * Created by ex3ndr on 14.09.14.
 */
public class TypedFutureError {
    private int id;
    private Throwable t;

    public TypedFutureError(int id, Throwable t) {
        this.id = id;
        this.t = t;
    }

    public int getId() {
        return id;
    }

    public Throwable getT() {
        return t;
    }
}
