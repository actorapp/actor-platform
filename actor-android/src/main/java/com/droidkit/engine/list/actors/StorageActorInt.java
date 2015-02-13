package com.droidkit.engine.list.actors;

import com.droidkit.engine.list.LoadCallback;
import com.droidkit.engine.list.LoadCenterCallback;
import com.droidkit.engine.list.LoadItemCallback;

import java.util.List;

/**
 * Created by ex3ndr on 11.09.14.
 */
public interface StorageActorInt<V> {

    public void updateOrAdd(V value);

    public void updateOrAdd(List<V> values);

    public void delete(long[] keys);

    public void delete(long key);

    public void clear();

    public void loadCenterInitial(long centerKey, int limit, LoadCenterCallback<V> callback);

    public void loadTailInitial(int limit, LoadCallback<V> callback);

    public void loadTail(Object key, int limit, LoadCallback<V> callback);

    public void loadHeadInitial(int limit, LoadCallback<V> callback);

    public void loadHead(Object key, int limit, LoadCallback<V> callback);

    public void loadTailInitial(String query, int limit, LoadCallback<V> callback);

    public void loadTail(String query, Object key, int limit, LoadCallback<V> callback);

    public void loadHeadInitial(String query, int limit, LoadCallback<V> callback);

    public void loadHead(String query, Object key, int limit, LoadCallback<V> callback);

    public void loadItem(long id, LoadItemCallback<V> callback);

    public void loadHeadValue(LoadItemCallback<V> callback);

    public void loadCount(LoadItemCallback<Integer> callback);
}
