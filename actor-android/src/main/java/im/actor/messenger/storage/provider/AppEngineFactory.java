package im.actor.messenger.storage.provider;

import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.storage.SQLiteProvider;
import im.actor.messenger.storage.sqlite.SQLiteKeyValue;
import im.actor.model.Storage;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.storage.KeyValueStorage;
import im.actor.model.storage.ListEngine;
import im.actor.model.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class AppEngineFactory implements Storage {
    @Override
    public PreferencesStorage createPreferencesStorage() {
        return new PropertiesProvider();
    }

    @Override
    public KeyValueStorage createUsersEngine() {
        return new SQLiteKeyValue(SQLiteProvider.db(), "actor_users");
    }

    @Override
    public KeyValueStorage createGroupsEngine() {
        return new SQLiteKeyValue(SQLiteProvider.db(), "actor_groups");
    }

    @Override
    public ListEngine<Contact> createContactsEngine() {
        return new ListProvider<Contact>(ListEngines.getContactsListEngine());
    }

    @Override
    public ListEngine<Dialog> createDialogsEngine() {
        return new ListProvider<Dialog>(ListEngines.getChatsListEngine());
    }

    @Override
    public ListEngine<Message> createMessagesEngine(Peer peer) {
        return new ListProvider<Message>(ListEngines.getMessages(peer));
    }
}
