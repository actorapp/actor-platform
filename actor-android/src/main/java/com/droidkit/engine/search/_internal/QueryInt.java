package com.droidkit.engine.search._internal;

import com.droidkit.engine.common.ValuesCallback;

/**
 * Created by ex3ndr on 19.09.14.
 */
public interface QueryInt<T> {
    public void index(long key, long order, String searchQuery, T data);

    public void indexLow(long key, long order, String searchQuery, T data);

    public void clear();

    public void remove(long key);

    public void query(String request, ValuesCallback<T> res);
}
