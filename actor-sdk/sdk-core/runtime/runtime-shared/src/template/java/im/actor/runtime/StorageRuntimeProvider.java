package im.actor.runtime;

import im.actor.runtime.storage.IndexStorage;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListStorage;
import im.actor.runtime.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class StorageRuntimeProvider implements StorageRuntime {

    @Override
    public PreferencesStorage createPreferencesStorage() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public IndexStorage createIndex(String name) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public KeyValueStorage createKeyValue(String name) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public ListStorage createList(String name) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public void resetStorage() {
        throw new RuntimeException("Dumb");
    }
}
