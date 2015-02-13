package com.droidkit.engine.list.storage;

/**
 * Created by ex3ndr on 10.09.14.
 */
class SqlKey {
    private long sortingKey;

    public SqlKey(long sortingKey) {
        this.sortingKey = sortingKey;
    }

    public long getSortingKey() {
        return sortingKey;
    }

    @Override
    public String toString() {
        return "{sort:" + sortingKey + "}";
    }
}
