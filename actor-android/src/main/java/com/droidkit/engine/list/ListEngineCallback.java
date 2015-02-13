package com.droidkit.engine.list;

import java.util.List;

/**
 * Created by ex3ndr on 21.09.14.
 */
public interface ListEngineCallback<T> {
    public void onItemRemoved(long id);

    public void onItemsRemoved(long[] ids);

    public void addOrUpdate(T item);

    public void addOrUpdate(List<T> items);

    public void onItemsReplaced(List<T> items);

    public void onListClear();
}
