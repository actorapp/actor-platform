package com.droidkit.engine.search;

/**
 * Created by ex3ndr on 19.09.14.
 */
public interface DataAdapter<T> {
    public T deserialize(byte[] data);

    public byte[] serialize(T data);
}
