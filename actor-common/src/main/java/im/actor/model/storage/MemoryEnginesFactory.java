package im.actor.model.storage;

import im.actor.model.Storage;
import im.actor.model.entity.*;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class MemoryEnginesFactory implements Storage {

    @Override
    public PreferencesStorage createPreferencesStorage() {
        return new MemoryPreferences();
    }

    @Override
    public KeyValueEngine<User> createUsersEngine() {
        return new MemoryKeyValueEngine<User>();
    }

    @Override
    public KeyValueEngine<ReadState> createReadStateEngine() {
        return new MemoryKeyValueEngine<ReadState>();
    }

    @Override
    public ListEngine<Dialog> createDialogsEngine() {
        return new MemoryListEngine<Dialog>();
    }

    @Override
    public ListEngine<Message> createMessagesEngine(Peer peer) {
        return new MemoryListEngine<Message>();
    }

}
