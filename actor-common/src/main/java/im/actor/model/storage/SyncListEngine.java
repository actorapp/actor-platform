package im.actor.model.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserCreator;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.engine.ListEngineCallback;
import im.actor.model.droidkit.engine.ListEngineDisplayExt;
import im.actor.model.droidkit.engine.ListEngineDisplayListener;
import im.actor.model.droidkit.engine.ListEngineItem;
import im.actor.model.droidkit.engine.ListEngineRecord;
import im.actor.model.droidkit.engine.ListStorage;

/**
 * Created by ex3ndr on 14.03.15.
 */
public class SyncListEngine<T extends BserObject & ListEngineItem>
        implements ListEngineDisplayExt<T> {

    private final ListStorage storage;
    private final BserCreator<T> creator;
    private final ObjectCache<Long, T> cache = new ObjectCache<Long, T>();
    private final Object LOCK = new Object();

    public SyncListEngine(ListStorage storage, BserCreator<T> creator) {
        this.storage = storage;
        this.creator = creator;
    }

    // Main List Engine

    @Override
    public void addOrUpdateItem(T item) {
        synchronized (LOCK) {
            // Update memory cache
            cache.onObjectUpdated(item.getEngineId(), item);
            storage.updateOrAdd(new ListEngineRecord(item.getEngineId(),
                    item.getEngineSort(), null, item.toByteArray()));
        }
    }

    @Override
    public void addOrUpdateItems(List<T> items) {
        synchronized (LOCK) {
            List<ListEngineRecord> updated = new ArrayList<ListEngineRecord>();
            for (T i : items) {
                // Update memory cache
                cache.onObjectUpdated(i.getEngineId(), i);

                updated.add(new ListEngineRecord(i.getEngineId(), i.getEngineSort(),
                        null, i.toByteArray()));
            }
            storage.updateOrAdd(updated);
        }
    }

    @Override
    public void replaceItems(List<T> items) {
        synchronized (LOCK) {
            cache.clear();
            storage.clear();

            List<ListEngineRecord> updated = new ArrayList<ListEngineRecord>();
            for (T i : items) {
                // Update memory cache
                cache.onObjectUpdated(i.getEngineId(), i);

                updated.add(new ListEngineRecord(i.getEngineId(), i.getEngineSort(),
                        null, i.toByteArray()));
            }
            storage.updateOrAdd(updated);
        }
    }

    @Override
    public void removeItem(long key) {
        synchronized (LOCK) {
            cache.removeObject(key);
            storage.delete(key);
        }
    }

    @Override
    public void removeItems(long[] keys) {
        synchronized (LOCK) {
            for (long key : keys) {
                cache.removeObject(key);
            }
            storage.delete(keys);
        }
    }

    @Override
    public void clear() {
        synchronized (LOCK) {
            cache.clear();
            storage.clear();
        }
    }

    @Override
    public T getValue(long key) {
        synchronized (LOCK) {
            T res = cache.lookup(key);
            if (res != null) {
                return res;
            }

            ListEngineRecord record = storage.loadItem(key);
            if (record == null) {
                return null;
            }

            try {
                res = Bser.parse(creator.createInstance(), record.getData());
                cache.onObjectLoaded(key, res);
                return res;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public T getHeadValue() {
        synchronized (LOCK) {
            List<ListEngineRecord> records = storage.loadAfter(0L, 1);
            if (records.size() != 1) {
                return null;
            }
            ListEngineRecord record = records.get(0);
            try {
                T res = Bser.parse(creator.createInstance(), record.getData());
                cache.onObjectLoaded(res.getEngineId(), res);
                return res;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public int getCount() {
        synchronized (LOCK) {
            return storage.getCount();
        }
    }

    // Display extension

    @Override
    public void subscribe(ListEngineDisplayListener<T> listener) {

    }

    @Override
    public void unsubscribe(ListEngineDisplayListener<T> listener) {

    }

    @Override
    public void loadTop(int limit, ListEngineCallback<T> callback) {
        ArrayList<T> res;
        synchronized (LOCK) {
            res = convertList(storage.loadAfter(0L, limit));
        }
        callCallback(callback, res);
    }

    @Override
    public void loadTop(long afterSortKey, int limit, ListEngineCallback<T> callback) {
        ArrayList<T> res;
        synchronized (LOCK) {
            res = convertList(storage.loadAfter(afterSortKey, limit));
        }
        callCallback(callback, res);
    }

    @Override
    public void loadTop(String query, int limit, ListEngineCallback<T> callback) {
        ArrayList<T> res;
        synchronized (LOCK) {
            res = convertList(storage.loadAfter(query, 0L, limit));
        }
        callCallback(callback, res);
    }

    @Override
    public void loadTop(String query, long afterSortKey, int limit, ListEngineCallback<T> callback) {
        ArrayList<T> res;
        synchronized (LOCK) {
            res = convertList(storage.loadAfter(query, afterSortKey, limit));
        }
        callCallback(callback, res);
    }

    @Override
    public void loadBottom(int limit, ListEngineCallback<T> callback) {
        ArrayList<T> res;
        synchronized (LOCK) {
            res = convertList(storage.loadBefore(0L, limit));
        }
        callCallback(callback, res);
    }

    @Override
    public void loadBottom(long beforeSortKey, int limit, ListEngineCallback<T> callback) {
        ArrayList<T> res;
        synchronized (LOCK) {
            res = convertList(storage.loadBefore(beforeSortKey, limit));
        }
        callCallback(callback, res);
    }

    @Override
    public void loadBottom(String query, int limit, ListEngineCallback<T> callback) {
        ArrayList<T> res;
        synchronized (LOCK) {
            res = convertList(storage.loadBefore(query, 0L, limit));
        }
        callCallback(callback, res);
    }

    @Override
    public void loadBottom(String query, long beforeSortKey, int limit, ListEngineCallback<T> callback) {
        ArrayList<T> res;
        synchronized (LOCK) {
            res = convertList(storage.loadBefore(query, beforeSortKey, limit));
        }
        callCallback(callback, res);
    }

    @Override
    public void loadCenter(long centerSortKey, int limit, ListEngineCallback<T> callback) {
        throw new RuntimeException("I Don't care");
    }

    private void callCallback(ListEngineCallback<T> callback, List<T> res) {
        if (res.size() == 0) {
            callback.onLoaded(res, 0, 0);
        } else {
            long topSort, bottomSort;
            topSort = bottomSort = res.get(0).getEngineSort();

            for (T t : res) {
                long sort = t.getEngineSort();
                if (topSort < sort) {
                    topSort = sort;
                }
                if (bottomSort > sort) {
                    bottomSort = sort;
                }
            }

            callback.onLoaded(res, topSort, bottomSort);
        }
    }

    private ArrayList<T> convertList(List<ListEngineRecord> records) {
        ArrayList<T> res = new ArrayList<T>();
        for (ListEngineRecord record : records) {
            try {
                T loaded = Bser.parse(creator.createInstance(), record.getData());
                cache.onObjectLoaded(loaded.getEngineId(), loaded);
                res.add(loaded);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }
}