package im.actor.messenger.storage.provider;

import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.KeyValueEngines;
import im.actor.messenger.storage.ListEngines;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.ReadState;
import im.actor.model.entity.User;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;
import im.actor.model.Storage;
import im.actor.model.storage.MemoryKeyValueEngine;
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
    public KeyValueEngine<User> createUsersEngine() {
        return new BoxerProvider<UserModel, User>(KeyValueEngines.users());
    }

    @Override
    public KeyValueEngine<ReadState> createReadStateEngine() {
        return new MemoryKeyValueEngine<ReadState>();
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
