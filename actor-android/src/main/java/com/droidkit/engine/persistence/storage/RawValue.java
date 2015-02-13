package com.droidkit.engine.persistence.storage;

/**
 * Created by ex3ndr on 26.09.14.
 */
public class RawValue {
    private long key;
    private byte[] data;

    public RawValue(long key, byte[] data) {
        this.key = key;
        this.data = data;
    }

    public long getKey() {
        return key;
    }

    public byte[] getData() {
        return data;
    }
}
