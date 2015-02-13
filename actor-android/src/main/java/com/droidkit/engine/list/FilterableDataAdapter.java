package com.droidkit.engine.list;

/**
 * Created by ex3ndr on 04.10.14.
 */
public interface FilterableDataAdapter<V> extends DataAdapter<V> {
    public String getFilterValue(V value);
}
