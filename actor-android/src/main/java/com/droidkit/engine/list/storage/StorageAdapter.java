package com.droidkit.engine.list.storage;

import java.util.List;

public interface StorageAdapter {

    // Modification

    public void updateOrAdd(ValueContainer valueContainer);

    public void updateOrAdd(List<ValueContainer> items);

    public void delete(long id);

    public void delete(long[] ids);

    public void clear();

    // Reading

    public ValueContainer loadItem(long id);

    public SliceResult<ValueContainer> loadHead(Object bottomSortingKey, int limit);

    public SliceResult<ValueContainer> loadTail(Object topSortingKey, int limit);

    public SliceResult<ValueContainer> loadHead(String query, Object bottomSortingKey, int limit);

    public SliceResult<ValueContainer> loadTail(String query, Object topSortingKey, int limit);

    public SliceResult<ValueContainer> loadBefore(long sortingKey, int limit);

    public SliceResult<ValueContainer> loadAfter(long sortingKey, int limit);

    public int getCount();
}