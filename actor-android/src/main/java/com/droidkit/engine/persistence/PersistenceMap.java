package com.droidkit.engine.persistence;

import android.support.annotation.NonNull;

import com.droidkit.engine.persistence.storage.PersistenceStorage;
import com.droidkit.engine.persistence.storage.RawValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by ex3ndr on 26.09.14.
 */
public abstract class PersistenceMap<V> implements Map<Long, V> {
    private Map<Long, V> backedMap;
    private PersistenceStorage storage;

    public PersistenceMap(PersistenceStorage storage) {
        this(storage, new HashMap<Long, V>());
    }

    public PersistenceMap(PersistenceStorage storage, Map<Long, V> backedMap) {
        this.storage = storage;
        this.backedMap = backedMap;
    }

    protected void init() {
        RawValue[] rawValues = storage.readAll();
        for (RawValue rawValue : rawValues) {
            V res = deserialize(rawValue.getData());
            if (res != null) {
                this.backedMap.put(rawValue.getKey(), res);
            }
        }
    }

    protected abstract byte[] serialize(V value);

    protected abstract V deserialize(byte[] value);

    @Override
    public void clear() {
        storage.clear();
        backedMap.clear();
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return backedMap.containsKey(key);
    }

    @Override
    public synchronized boolean containsValue(Object value) {
        return backedMap.containsValue(value);
    }

    @Override
    public synchronized Set<Entry<Long, V>> entrySet() {
        return backedMap.entrySet();
    }

    @Override
    public synchronized V get(Object key) {
        return backedMap.get(key);
    }

    @Override
    public synchronized boolean isEmpty() {
        return backedMap.isEmpty();
    }

    @Override
    public synchronized Set<Long> keySet() {
        return backedMap.keySet();
    }

    @Override
    public synchronized V put(Long key, V value) {
        storage.put(new RawValue(key, serialize(value)));
        return backedMap.put(key, value);
    }

    @Override
    public synchronized void putAll(Map<? extends Long, ? extends V> map) {
        for (Map.Entry<? extends Long, ? extends V> entry : map.entrySet()) {
            storage.put(new RawValue(entry.getKey(), serialize(entry.getValue())));
            backedMap.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public synchronized V remove(Object key) {
        if (key != null && key instanceof Long) {
            storage.remove((Long) key);
        }
        return backedMap.remove(key);
    }

    @Override
    public synchronized int size() {
        return backedMap.size();
    }

    @Override
    public Collection<V> values() {
        return backedMap.values();
    }
}
