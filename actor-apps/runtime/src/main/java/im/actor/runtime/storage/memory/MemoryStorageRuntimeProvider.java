package im.actor.runtime.storage.memory;


import im.actor.runtime.StorageRuntime;
import im.actor.runtime.storage.IndexStorage;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListStorage;
import im.actor.runtime.storage.PreferencesStorage;

public class MemoryStorageRuntimeProvider implements StorageRuntime {

    @Override
    public PreferencesStorage createPreferencesStorage() {
        return new MemoryPreferencesStorage();
    }

    @Override
    public IndexStorage createIndex(String name) {
        return new MemoryIndexStorage();
    }

    @Override
    public KeyValueStorage createKeyValue(String name) {
        return new MemoryKeyValueStorage();
    }

    @Override
    public ListStorage createList(String name) {
        return new MemoryListStorage();
    }

    @Override
    public void resetStorage() {
        // TODO: Implement
    }
}
