package im.actor.model.storage.temp;

import im.actor.model.Storage;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.storage.KeyValueStorage;
import im.actor.model.storage.ListEngine;
import im.actor.model.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 23.02.15.
 */
public class TempStorage implements Storage {
    @Override
    public PreferencesStorage createPreferencesStorage() {
        return new TempPreferences();
    }

    @Override
    public KeyValueStorage createUsersEngine() {
        return new TempKeyValueStorage();
    }

    @Override
    public KeyValueStorage createGroupsEngine() {
        return new TempKeyValueStorage();
    }

    @Override
    public KeyValueStorage createDownloadsEngine() {
        return new TempKeyValueStorage();
    }

    @Override
    public ListEngine<Contact> createContactsEngine() {
        return new TempListEngine<Contact>();
    }

    @Override
    public ListEngine<Dialog> createDialogsEngine() {
        return new TempListEngine<Dialog>();
    }

    @Override
    public ListEngine<Message> createMessagesEngine(Peer peer) {
        return new TempListEngine<Message>();
    }
}
