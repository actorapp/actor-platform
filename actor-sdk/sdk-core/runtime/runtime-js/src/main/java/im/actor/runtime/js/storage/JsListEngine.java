/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.runtime.Log;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.ListEngineRecord;

public class JsListEngine<T extends BserObject & ListEngineItem> implements ListEngine<T> {

    private JsListStorage storage;
    private BserCreator<T> creator;
    private HashMap<Long, T> cache = new HashMap<Long, T>();
    private ArrayList<JsListEngineCallback<T>> callbacks = new ArrayList<JsListEngineCallback<T>>();

    public JsListEngine(JsListStorage storage, BserCreator<T> creator) {
        this.storage = storage;
        this.creator = creator;
    }

    @Override
    public void addOrUpdateItem(T item) {

        cache.put(item.getEngineId(), item);
        storage.updateOrAdd(new ListEngineRecord(item.getEngineId(), item.getEngineSort(),
                item.getEngineSearch(), item.toByteArray()));

        for (JsListEngineCallback<T> callback : callbacks) {
            try {
                callback.onItemAddedOrUpdated(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void addOrUpdateItems(List<T> items) {

        ArrayList<ListEngineRecord> records = new ArrayList<ListEngineRecord>();
        for (T t : items) {
            cache.put(t.getEngineId(), t);
            records.add(new ListEngineRecord(t.getEngineId(), t.getEngineSort(), t.getEngineSearch(),
                    t.toByteArray()));
        }
        storage.updateOrAdd(records);

        for (JsListEngineCallback<T> callback : callbacks) {
            try {
                callback.onItemsAddedOrUpdated(items);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void replaceItems(List<T> items) {
        cache.clear();
        storage.clear();
        ArrayList<ListEngineRecord> records = new ArrayList<ListEngineRecord>();
        for (T t : items) {
            cache.put(t.getEngineId(), t);
            records.add(new ListEngineRecord(t.getEngineId(), t.getEngineSort(), t.getEngineSearch(),
                    t.toByteArray()));
        }
        storage.updateOrAdd(records);

        for (JsListEngineCallback<T> callback : callbacks) {
            try {
                callback.onItemsReplaced(items);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeItem(long key) {
        cache.remove(key);
        storage.delete(key);

        for (JsListEngineCallback<T> callback : callbacks) {
            try {
                callback.onItemRemoved(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeItems(long[] keys) {
        for (long key : keys) {
            cache.remove(key);
        }
        storage.delete(keys);
        for (JsListEngineCallback<T> callback : callbacks) {
            try {
                callback.onItemsRemoved(keys);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void clear() {
        cache.clear();
        storage.clear();
        for (JsListEngineCallback<T> callback : callbacks) {
            try {
                callback.onClear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T getValue(long key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        ListEngineRecord record = storage.loadItem(key);
        if (record != null) {
            try {
                T res = Bser.parse(creator.createInstance(), record.getData());
                cache.put(key, res);
                return res;
            } catch (IOException e) {
                Log.d("JsListEngine", "Unable to decode: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public T getHeadValue() {
        Long id = storage.getHeadId();
        if (id != null) {
            return getValue(id);
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public int getCount() {
        return storage.getCount();
    }

    public long[] getOrderedIds() {
        return storage.getOrderedIds();
    }

    public void addListener(JsListEngineCallback<T> callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public void removeListener(JsListEngineCallback<T> callback) {
        callbacks.remove(callback);
    }
}
