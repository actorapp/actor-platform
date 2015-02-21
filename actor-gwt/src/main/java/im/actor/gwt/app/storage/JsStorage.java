package im.actor.gwt.app.storage;

import im.actor.model.Storage;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.ReadState;
import im.actor.model.storage.KeyValueEngine;
import im.actor.model.storage.KeyValueStorage;
import im.actor.model.storage.ListEngine;
import im.actor.model.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 21.02.15.
 */
public class JsStorage implements Storage {

    com.google.gwt.storage.client.Storage storage;

    public JsStorage() {
        this.storage = com.google.gwt.storage.client.Storage.getLocalStorageIfSupported();
    }

    public boolean isLocalStorageSupported() {
        return storage != null;
    }

    @Override
    public PreferencesStorage createPreferencesStorage() {
        if (isLocalStorageSupported()) {
            return new LocalStoragePreferences(storage);
        } else {
            return new MemoryPreferences();
        }
    }

    @Override
    public KeyValueStorage createUsersEngine() {
        if (isLocalStorageSupported()) {
            return new JsKeyValue(storage, "user");
        } else {
            return new MemoryKeyValue();
        }
    }

    @Override
    public KeyValueEngine<ReadState> createReadStateEngine() {
        return null;
    }

    @Override
    public ListEngine<Dialog> createDialogsEngine() {
        return null;
    }

    @Override
    public ListEngine<Message> createMessagesEngine(Peer peer) {
        return null;
    }
}
