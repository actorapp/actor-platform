/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.storage;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserCreator;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListEngineItem;
import im.actor.model.droidkit.engine.ListEngineRecord;
import im.actor.model.js.providers.JsLogProvider;
import im.actor.model.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            callback.onItemAddedOrUpdated(item);
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

        for (T t : items) {
            for (JsListEngineCallback<T> callback : callbacks) {
                callback.onItemAddedOrUpdated(t);
            }
        }
    }

    @Override
    public void replaceItems(List<T> items) {
        cache.clear();
        storage.clear();
        for (JsListEngineCallback<T> callback : callbacks) {
            callback.onClear();
        }

        ArrayList<ListEngineRecord> records = new ArrayList<ListEngineRecord>();
        for (T t : items) {
            cache.put(t.getEngineId(), t);
            records.add(new ListEngineRecord(t.getEngineId(), t.getEngineSort(), t.getEngineSearch(),
                    t.toByteArray()));
        }
        storage.updateOrAdd(records);

        for (T t : items) {
            for (JsListEngineCallback<T> callback : callbacks) {
                callback.onItemAddedOrUpdated(t);
            }
        }
    }

    @Override
    public void removeItem(long key) {
        cache.remove(key);
        storage.delete(key);
        for (JsListEngineCallback<T> callback : callbacks) {
            callback.onItemRemoved(key);
        }
    }

    @Override
    public void removeItems(long[] keys) {
        for (long key : keys) {
            cache.remove(key);
        }
        storage.delete(keys);
        for (long key : keys) {
            for (JsListEngineCallback<T> callback : callbacks) {
                callback.onItemRemoved(key);
            }
        }
    }

    @Override
    public void clear() {
        cache.clear();
        storage.clear();
        for (JsListEngineCallback<T> callback : callbacks) {
            callback.onClear();
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
