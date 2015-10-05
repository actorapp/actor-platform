/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.storage.ListEngineDisplayExt;
import im.actor.runtime.storage.ListEngineDisplayListener;
import im.actor.runtime.storage.ListEngineDisplayLoadCallback;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.ListStorageDisplayEx;
import im.actor.runtime.storage.ObjectCache;

public class AsyncListEngine<T extends BserObject & ListEngineItem>
        implements ListEngineDisplayExt<T> {

    private final AsyncStorageInt<T> asyncStorageInt;
    private final ObjectCache<Long, T> cache = new ObjectCache<Long, T>();
    private final Object LOCK = new Object();
    private CopyOnWriteArrayList<ListEngineDisplayListener<T>> listeners = new CopyOnWriteArrayList<ListEngineDisplayListener<T>>();

    public AsyncListEngine(ListStorageDisplayEx storage, BserCreator<T> creator) {
        this.asyncStorageInt = new AsyncStorageInt<T>(storage, creator);
    }

    // Main List Engine

    @Override
    public void addOrUpdateItem(T item) {
        synchronized (LOCK) {
            // Update memory cache
            cache.onObjectUpdated(item.getEngineId(), item);

            List<T> items = new ArrayList<T>();
            items.add(item);
            asyncStorageInt.addOrUpdateItems(items);

            for (ListEngineDisplayListener<T> l : listeners) {
                l.addOrUpdate(item);
            }
        }
    }

    @Override
    public void addOrUpdateItems(List<T> items) {
        synchronized (LOCK) {

            // Update memory cache
            for (T i : items) {
                cache.onObjectUpdated(i.getEngineId(), i);
            }

            asyncStorageInt.addOrUpdateItems(items);

            for (ListEngineDisplayListener<T> l : listeners) {
                l.addOrUpdate(items);
            }
        }
    }

    @Override
    public void replaceItems(List<T> items) {
        synchronized (LOCK) {

            // Update memory cache
            cache.clear();
            for (T i : items) {
                cache.onObjectUpdated(i.getEngineId(), i);
            }

            asyncStorageInt.replaceItems(items);


            for (ListEngineDisplayListener<T> l : listeners) {
                l.onItemsReplaced(items);
            }
        }
    }

    @Override
    public void removeItem(long key) {
        synchronized (LOCK) {
            cache.removeObject(key);
            asyncStorageInt.remove(new long[]{key});

            for (ListEngineDisplayListener<T> l : listeners) {
                l.onItemRemoved(key);
            }
        }
    }

    @Override
    public void removeItems(long[] keys) {
        synchronized (LOCK) {
            for (long key : keys) {
                cache.removeObject(key);
            }
            asyncStorageInt.remove(keys);

            for (ListEngineDisplayListener<T> l : listeners) {
                l.onItemsRemoved(keys);
            }
        }
    }

    @Override
    public void clear() {
        synchronized (LOCK) {
            cache.clear();
            asyncStorageInt.clear();

            for (ListEngineDisplayListener<T> l : listeners) {
                l.onListClear();
            }
        }
    }

    @Override
    public T getValue(long key) {
        synchronized (LOCK) {
            T res = cache.lookup(key);
            if (res != null) {
                return res;
            }
        }

        T res = asyncStorageInt.getValue(key);

        if (res != null) {
            synchronized (LOCK) {
                cache.onObjectLoaded(key, res);
            }
        }
        return res;

    }

    @Override
    public T getHeadValue() {
        T res = asyncStorageInt.getHeadValue();

        if (res != null) {
            synchronized (LOCK) {
                cache.onObjectLoaded(res.getEngineId(), res);
            }
        }

        return res;
    }

    @Override
    public boolean isEmpty() {
        // TODO: Correct implementation
        return getCount() == 0;
    }

    @Override
    public int getCount() {
        return asyncStorageInt.getCount();
    }

    // Display extension

    @Override
    public void subscribe(ListEngineDisplayListener<T> listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void unsubscribe(ListEngineDisplayListener<T> listener) {
        listeners.remove(listener);
    }

    @Override
    public void loadForward(int limit, ListEngineDisplayLoadCallback<T> callback) {
        asyncStorageInt.loadForward(null, null, limit, cover(callback));
    }

    @Override
    public void loadForward(long afterSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
        asyncStorageInt.loadForward(null, afterSortKey, limit, cover(callback));
    }

    @Override
    public void loadForward(String query, int limit, ListEngineDisplayLoadCallback<T> callback) {
        asyncStorageInt.loadForward(query, null, limit, cover(callback));
    }

    @Override
    public void loadForward(String query, long afterSortKey, int limit, final ListEngineDisplayLoadCallback<T> callback) {
        asyncStorageInt.loadForward(query, afterSortKey, limit, cover(callback));
    }

    @Override
    public void loadBackward(int limit, ListEngineDisplayLoadCallback<T> callback) {
        asyncStorageInt.loadBackward(null, null, limit, cover(callback));
    }

    @Override
    public void loadBackward(long beforeSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
        asyncStorageInt.loadBackward(null, beforeSortKey, limit, cover(callback));
    }

    @Override
    public void loadBackward(String query, int limit, ListEngineDisplayLoadCallback<T> callback) {
        asyncStorageInt.loadBackward(query, null, limit, cover(callback));
    }

    @Override
    public void loadBackward(String query, long beforeSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
        asyncStorageInt.loadBackward(query, beforeSortKey, limit, cover(callback));
    }

    @Override
    public void loadCenter(long centerSortKey, int limit, ListEngineDisplayLoadCallback<T> callback) {
        asyncStorageInt.loadCenter(centerSortKey, limit, cover(callback));
    }

    private ListEngineDisplayLoadCallback<T> cover(final ListEngineDisplayLoadCallback<T> callback) {
        return new ListEngineDisplayLoadCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                synchronized (LOCK) {
                    for (T i : items) {
                        cache.onObjectLoaded(i.getEngineId(), i);
                    }
                }
                callback.onLoaded(items, topSortKey, bottomSortKey);
            }
        };
    }
}