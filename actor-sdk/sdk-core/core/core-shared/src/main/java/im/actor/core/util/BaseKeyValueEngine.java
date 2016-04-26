/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.KeyValueItem;
import im.actor.runtime.storage.KeyValueRecord;
import im.actor.runtime.storage.KeyValueStorage;

public abstract class BaseKeyValueEngine<T extends KeyValueItem> implements KeyValueEngine<T> {
    private final HashMap<Long, T> cache = new HashMap<>();

    private KeyValueStorage storage;

    protected BaseKeyValueEngine(KeyValueStorage storage) {
        this.storage = storage;
    }

    protected abstract byte[] serialize(T value);

    protected abstract T deserialize(byte[] data);

    @Override
    public synchronized void addOrUpdateItem(T item) {
        cache.put(item.getEngineId(), item);
        byte[] data = serialize(item);
        storage.addOrUpdateItem(item.getEngineId(), data);
    }

    @Override
    public synchronized void addOrUpdateItems(List<T> values) {
        for (T t : values) {
            cache.put(t.getEngineId(), t);
        }

        ArrayList<KeyValueRecord> records = new ArrayList<>();
        for (T v : values) {
            records.add(new KeyValueRecord(v.getEngineId(), serialize(v)));
        }
        storage.addOrUpdateItems(records);
    }

    @Override
    public synchronized void removeItem(long id) {
        cache.remove(id);
        storage.removeItem(id);
    }

    @Override
    public synchronized void removeItems(long[] ids) {
        for (long l : ids) {
            cache.remove(l);
        }
        storage.removeItems(ids);
    }

    @Override
    public synchronized void clear() {
        cache.clear();
        storage.clear();
    }

    @Override
    public synchronized T getValue(long id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        byte[] data = storage.loadItem(id);
        if (data != null) {
            T res = deserialize(data);
            if (res != null) {
                cache.put(res.getEngineId(), res);
                return res;
            }
        }
        return null;
    }

    @Override
    public Promise<T> getValueAsync(long key) {
        T res = getValue(key);
        if (res != null) {
            return Promise.success(res);
        } else {
            return Promise.failure(new RuntimeException());
        }
    }

    @Override
    public Promise<Boolean> containsAsync(long key) {
        T res = getValue(key);
        return Promise.success(res != null);
    }
}
