/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import im.actor.runtime.StorageRuntime;
import im.actor.runtime.js.storage.JsIndexStorage;
import im.actor.runtime.js.storage.JsKeyValueStorage;
import im.actor.runtime.js.storage.JsListStorage;
import im.actor.runtime.js.storage.JsPreferencesStorage;
import im.actor.runtime.storage.IndexStorage;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListStorage;
import im.actor.runtime.storage.PreferencesStorage;

public class JsStorageProvider implements StorageRuntime {

    private com.google.gwt.storage.client.Storage storage;

    public JsStorageProvider() {
        this.storage = com.google.gwt.storage.client.Storage.getLocalStorageIfSupported();
    }

    @Override
    public PreferencesStorage createPreferencesStorage() {
        return new JsPreferencesStorage(storage);
    }

    @Override
    public IndexStorage createIndex(String name) {
        return new JsIndexStorage(name, storage);
    }

    @Override
    public KeyValueStorage createKeyValue(String name) {
        return new JsKeyValueStorage(name, storage);
    }

    @Override
    public ListStorage createList(String name) {
        return new JsListStorage(name, storage);
    }

    @Override
    public void resetStorage() {
        storage.clear();
    }
}
