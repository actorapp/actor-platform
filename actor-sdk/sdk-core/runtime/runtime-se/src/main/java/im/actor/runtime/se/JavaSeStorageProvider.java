/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.se;

import im.actor.runtime.StorageRuntime;
import im.actor.runtime.storage.IndexStorage;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListStorage;
import im.actor.runtime.storage.PreferencesStorage;
import im.actor.runtime.storage.memory.MemoryKeyValueStorage;
import im.actor.runtime.storage.memory.MemoryListStorage;
import im.actor.runtime.storage.memory.MemoryPreferencesStorage;

public class JavaSeStorageProvider implements StorageRuntime {

    public JavaSeStorageProvider() {

    }

    @Override
    public PreferencesStorage createPreferencesStorage() {
        return new MemoryPreferencesStorage();
    }

    @Override
    public IndexStorage createIndex(String name) {
        throw new RuntimeException();
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
        
    }
}
