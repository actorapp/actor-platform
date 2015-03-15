package im.actor.model.android;

import im.actor.model.android.sql.SQLiteList;
import im.actor.model.android.sql.SQLiteProvider;
import im.actor.model.android.sql.SQLiteKeyValue;
import im.actor.model.Storage;
import im.actor.model.droidkit.engine.KeyValueStorage;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListStorage;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.droidkit.engine.PreferencesStorage;
import im.actor.model.droidkit.engine.AsyncListEngine;

/**
 * Created by ex3ndr on 14.03.15.
 */
public class AndroidStorage implements Storage {

    @Override
    public PreferencesStorage createPreferencesStorage() {
        return new AndroidProperties();
    }

    @Override
    public KeyValueStorage createKeyValue(String name) {
        return new SQLiteKeyValue(SQLiteProvider.db(), "kv_" + name);
    }

    @Override
    public ListStorage createList(String name) {
        return new SQLiteList(SQLiteProvider.db(), "ls_" + name);
    }

    @Override
    public ListEngine<Contact> createContactsList(ListStorage storage) {
        return new AsyncListEngine<Contact>(storage, Contact.CREATOR);
    }

    @Override
    public ListEngine<Dialog> createDialogsList(ListStorage storage) {
        return new AsyncListEngine<Dialog>(storage, Dialog.CREATOR);
    }

    @Override
    public ListEngine<Message> createMessagesList(Peer peer, ListStorage storage) {
        return new AsyncListEngine<Message>(storage, Message.CREATOR);
    }
}
