package im.actor.runtime.cocoa;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.StorageRuntime;
import im.actor.runtime.storage.IndexStorage;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListStorage;
import im.actor.runtime.storage.PreferencesStorage;

public class CocoaStorageProxyProvider implements StorageRuntime {

    private static StorageRuntime storageRuntime;

    @ObjectiveCName("setStorageRuntime:")
    public static void setStorageRuntime(StorageRuntime storageRuntime) {
        CocoaStorageProxyProvider.storageRuntime = storageRuntime;
    }

    @Override
    public PreferencesStorage createPreferencesStorage() {
        if (storageRuntime == null) {
            throw new RuntimeException("Storage Runtime not set");
        }
        return storageRuntime.createPreferencesStorage();
    }

    @Override
    public IndexStorage createIndex(String name) {
        if (storageRuntime == null) {
            throw new RuntimeException("Storage Runtime not set");
        }
        return storageRuntime.createIndex(name);
    }

    @Override
    public KeyValueStorage createKeyValue(String name) {
        if (storageRuntime == null) {
            throw new RuntimeException("Storage Runtime not set");
        }
        return storageRuntime.createKeyValue(name);
    }

    @Override
    public ListStorage createList(String name) {
        if (storageRuntime == null) {
            throw new RuntimeException("Storage Runtime not set");
        }
        return storageRuntime.createList(name);
    }

    @Override
    public void resetStorage() {
        if (storageRuntime == null) {
            throw new RuntimeException("Storage Runtime not set");
        }
        storageRuntime.resetStorage();
    }
}
