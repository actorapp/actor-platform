package im.actor.gwt.app.storage;

import java.io.IOException;

import im.actor.model.Storage;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
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
            return new JsPreferences(storage);
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
    public KeyValueStorage createGroupsEngine() {
        if (isLocalStorageSupported()) {
            return new JsKeyValue(storage, "group");
        } else {
            return new MemoryKeyValue();
        }
    }

    @Override
    public KeyValueStorage createDownloadsEngine() {
        if (isLocalStorageSupported()) {
            return new JsKeyValue(storage, "downloads");
        } else {
            return new MemoryKeyValue();
        }
    }

    @Override
    public ListEngine<Contact> createContactsEngine() {
        return new JsListEngine<Contact>("сontacts", storage) {
            @Override
            protected byte[] serialize(Contact item) {
                return item.toByteArray();
            }

            @Override
            protected Contact deserialize(byte[] data) {
                try {
                    return Contact.fromBytes(data);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public ListEngine<Dialog> createDialogsEngine() {
        return new JsListEngine<Dialog>("dialogs", storage) {
            @Override
            protected byte[] serialize(Dialog item) {
                return item.toByteArray();
            }

            @Override
            protected Dialog deserialize(byte[] data) {
                try {
                    return Dialog.fromBytes(data);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public ListEngine<Message> createMessagesEngine(Peer peer) {
        return new JsListEngine<Message>("msg" + peer.getUnuqueId(), storage) {
            @Override
            protected byte[] serialize(Message item) {
                return item.toByteArray();
            }

            @Override
            protected Message deserialize(byte[] data) {
                try {
                    return Message.fromBytes(data);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }
}
