/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

public class SyncKeyValue {

    private KeyValueStorage storage;

    public SyncKeyValue(KeyValueStorage storage) {
        this.storage = storage;
    }

    public synchronized void put(long key, byte[] data) {
        storage.addOrUpdateItem(key, data);
    }

    public synchronized void delete(long key) {
        storage.removeItem(key);
    }

    public synchronized byte[] get(long key) {
        return storage.getValue(key);
    }
}
