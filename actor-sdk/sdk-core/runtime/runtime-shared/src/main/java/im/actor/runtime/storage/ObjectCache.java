/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import java.util.HashMap;
import java.util.HashSet;

public class ObjectCache<K, V> {
    protected final HashMap<K, V> memoryCache = new HashMap<K, V>();
    protected final HashSet<K> removedItems = new HashSet<K>();
    private boolean lockLoading = false;

    public synchronized void onObjectLoaded(K key, V value) {
        if (lockLoading) {
            return;
        }
        if (removedItems.contains(key)) {
            return;
        }
        if (memoryCache.containsKey(key)) {
            return;
        }
        memoryCache.put(key, value);
    }

    public synchronized void onObjectUpdated(K key, V value) {
        removedItems.remove(key);
        memoryCache.put(key, value);
    }

    public synchronized void removeObject(K key) {
        memoryCache.remove(key);
        removedItems.add(key);
    }

    public synchronized V lookup(K key) {
        return memoryCache.get(key);
    }

    public synchronized void clear() {
        memoryCache.clear();
        removedItems.clear();
    }

    public synchronized void startLock(){
        lockLoading = true;
    }

    public synchronized void stopLock(){
        lockLoading = false;
    }
}
