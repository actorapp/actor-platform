/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers;

import im.actor.core.StorageProvider;
import im.actor.core.droidkit.engine.IndexStorage;
import im.actor.core.droidkit.engine.KeyValueStorage;
import im.actor.core.droidkit.engine.ListEngine;
import im.actor.core.droidkit.engine.ListStorage;
import im.actor.core.droidkit.engine.PreferencesStorage;
import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.SearchEntity;
import im.actor.core.js.providers.storage.JsIndexStorage;
import im.actor.core.js.providers.storage.JsKeyValueStorage;
import im.actor.core.js.providers.storage.JsListEngine;
import im.actor.core.js.providers.storage.JsListStorage;
import im.actor.core.js.providers.storage.JsPreferencesStorage;

public class JsStorageProvider implements StorageProvider {

    private com.google.gwt.storage.client.Storage storage;

    public JsStorageProvider() {
        this.storage = com.google.gwt.storage.client.Storage.getLocalStorageIfSupported();
    }

    public boolean isLocalStorageSupported() {
        return storage != null;
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
    public ListEngine<SearchEntity> createSearchList(ListStorage storage) {
        return new JsListEngine<SearchEntity>((JsListStorage) storage, SearchEntity.CREATOR);
    }

    @Override
    public ListEngine<Contact> createContactsList(ListStorage storage) {
        return new JsListEngine<Contact>((JsListStorage) storage, Contact.CREATOR);
    }

    @Override
    public ListEngine<Dialog> createDialogsList(ListStorage storage) {
        return new JsListEngine<Dialog>((JsListStorage) storage, Dialog.CREATOR);
    }

    @Override
    public ListEngine<Message> createMessagesList(Peer peer, ListStorage storage) {
        return new JsListEngine<Message>((JsListStorage) storage, Message.CREATOR);
    }

    @Override
    public void resetStorage() {
        storage.clear();
    }
}
