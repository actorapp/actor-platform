package com.droidkit.engine.keyvalue.sqlite;

/**
 * Created by ex3ndr on 10.09.14.
 */
public class DaoValue<V> {
    private long rowId;
    private long id;
    private long sortingKey;
    private V value;

    public DaoValue(long rowId, long id, long sortingKey, V value) {
        this.rowId = rowId;
        this.id = id;
        this.sortingKey = sortingKey;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public long getSortingKey() {
        return sortingKey;
    }

    public long getRowId() {
        return rowId;
    }

    public V getValue() {
        return value;
    }
}
