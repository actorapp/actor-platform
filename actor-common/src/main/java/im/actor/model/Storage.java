package im.actor.model;

import im.actor.model.entity.*;
import im.actor.model.storage.KeyValueEngine;
import im.actor.model.storage.KeyValueStorage;
import im.actor.model.storage.ListEngine;
import im.actor.model.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface Storage {

    public PreferencesStorage createPreferencesStorage();

    public KeyValueStorage createUsersEngine();

    public KeyValueEngine<ReadState> createReadStateEngine();

    public ListEngine<Dialog> createDialogsEngine();

    public ListEngine<Message> createMessagesEngine(Peer peer);
}