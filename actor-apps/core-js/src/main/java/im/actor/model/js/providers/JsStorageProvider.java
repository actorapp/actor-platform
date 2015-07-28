/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import im.actor.model.StorageProvider;
import im.actor.model.droidkit.engine.IndexStorage;
import im.actor.model.droidkit.engine.KeyValueStorage;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListStorage;
import im.actor.model.droidkit.engine.PreferencesStorage;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.SearchEntity;
import im.actor.model.js.providers.storage.JsIndexStorage;
import im.actor.model.js.providers.storage.JsKeyValueStorage;
import im.actor.model.js.providers.storage.JsListEngine;
import im.actor.model.js.providers.storage.JsListStorage;
import im.actor.model.js.providers.storage.JsPreferencesStorage;

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
