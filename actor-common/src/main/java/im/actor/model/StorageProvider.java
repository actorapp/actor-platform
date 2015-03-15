package im.actor.model;

import im.actor.model.droidkit.engine.KeyValueStorage;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListStorage;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.droidkit.engine.PreferencesStorage;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface StorageProvider {

    // Storage

    public PreferencesStorage createPreferencesStorage();

    public KeyValueStorage createKeyValue(String name);

    public ListStorage createList(String name);

    // Engines

    public ListEngine<Contact> createContactsList(ListStorage storage);

    public ListEngine<Dialog> createDialogsList(ListStorage storage);

    public ListEngine<Message> createMessagesList(Peer peer, ListStorage storage);
}