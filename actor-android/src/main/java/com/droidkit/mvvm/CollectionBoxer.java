package com.droidkit.mvvm;

import java.util.HashMap;

/**
 * Created by ex3ndr on 15.09.14.
 */
public abstract class CollectionBoxer<K, R, U> {

    private HashMap<K, U> cache = new HashMap<K, U>();

    public synchronized void put(K key, R raw) {
        if (cache.containsKey(key)) {
            U res = cache.get(key);
            update(key, raw, res);
            save(key, raw);
        } else {
            U res = wrap(key, raw);
            cache.put(key, res);
            save(key, raw);
        }
    }

    public synchronized U get(K key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        R raw = load(key);
        if (raw == null) {
            return null;
        }
        U res = wrap(key, raw);
        cache.put(key, res);
        return res;
    }

    protected abstract U wrap(K key, R raw);

    protected abstract R load(K key);

    protected abstract void save(K key, R raw);

    protected abstract void update(K key, R raw, U wrap);
}
