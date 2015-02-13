package com.droidkit.engine.persistence.storage;

/**
 * Created by ex3ndr on 26.09.14.
 */
public interface PersistenceStorage {
    public void put(RawValue value);

    public void put(RawValue[] values);

    public void remove(long key);

    public RawValue[] readAll();

    public void clear();
}
