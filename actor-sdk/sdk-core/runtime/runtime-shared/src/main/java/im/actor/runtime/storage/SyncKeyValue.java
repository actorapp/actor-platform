/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import java.util.HashMap;

public class SyncKeyValue {

    private KeyValueStorage storage;

    private HashMap<Long, byte[]> cached = new HashMap<Long, byte[]>();

    public SyncKeyValue(KeyValueStorage storage) {
        this.storage = storage;
    }

    public synchronized void put(long key, byte[] data) {
        storage.addOrUpdateItem(key, data);
        cached.put(key, data);
    }

    public synchronized void delete(long key) {
        storage.removeItem(key);
        cached.put(key, null);
    }

    public synchronized byte[] get(long key) {
        if (cached.containsKey(key)) {
            return cached.get(key);
        }
        return storage.loadItem(key);
    }
}