/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.se;

import im.actor.runtime.StorageRuntime;
import im.actor.runtime.storage.IndexStorage;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListStorage;
import im.actor.runtime.storage.PreferencesStorage;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class JavaSeStorageProvider implements StorageRuntime {

    public JavaSeStorageProvider() {

    }

    @Override
    public PreferencesStorage createPreferencesStorage() {
        throw new NotImplementedException();
    }

    @Override
    public IndexStorage createIndex(String name) {
        throw new NotImplementedException();
    }

    @Override
    public KeyValueStorage createKeyValue(String name) {
        throw new NotImplementedException();
    }

    @Override
    public ListStorage createList(String name) {
        throw new NotImplementedException();
    }

    @Override
    public void resetStorage() {
        throw new NotImplementedException();
    }
}
