package im.actor.runtime.cocoa;

import im.actor.runtime.StorageRuntime;
import im.actor.runtime.storage.IndexStorage;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListStorage;
import im.actor.runtime.storage.PreferencesStorage;

public class CocoaStorageProxyProvider implements StorageRuntime {

    private static StorageRuntime storageRuntime;

    public static void setStorageRuntime(StorageRuntime storageRuntime) {
        CocoaStorageProxyProvider.storageRuntime = storageRuntime;
    }

    @Override
    public PreferencesStorage createPreferencesStorage() {
        return storageRuntime.createPreferencesStorage();
    }

    @Override
    public IndexStorage createIndex(String name) {
        return storageRuntime.createIndex(name);
    }

    @Override
    public KeyValueStorage createKeyValue(String name) {
        return storageRuntime.createKeyValue(name);
    }

    @Override
    public ListStorage createList(String name) {
        return storageRuntime.createList(name);
    }

    @Override
    public void resetStorage() {
        storageRuntime.resetStorage();
    }
}
