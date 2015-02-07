package com.droidkit.actors.typed.messages;

/**
 * Created by ex3ndr on 14.09.14.
 */
public class TypedFutureResult {
    private int id;
    private Object res;

    public TypedFutureResult(int id, Object res) {
        this.id = id;
        this.res = res;
    }

    public int getId() {
        return id;
    }

    public Object getRes() {
        return res;
    }
}
