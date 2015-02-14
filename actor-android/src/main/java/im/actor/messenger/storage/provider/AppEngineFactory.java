package im.actor.messenger.storage.provider;

import im.actor.messenger.storage.KeyValueEngines;
import im.actor.messenger.storage.ListEngines;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PendingMessage;
import im.actor.model.entity.User;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;
import im.actor.model.storage.EnginesFactory;
import im.actor.model.storage.MemoryKeyValueEngine;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class AppEngineFactory implements EnginesFactory {
    @Override
    public KeyValueEngine<User> createUsersEngine() {
        return new KeyValueProvider<User>(KeyValueEngines.usersEngine());
    }

    @Override
    public ListEngine<Dialog> createDialogsEngine() {
        return new ListProvider<Dialog>(ListEngines.getChatsListEngine());
    }

    @Override
    public ListEngine<Message> createMessagesEngine(Peer peer) {
        return new ListProvider<Message>(ListEngines.getMessages(peer));
    }

    @Override
    public KeyValueEngine<PendingMessage> pendingMessages(Peer peer) {
        return new MemoryKeyValueEngine<PendingMessage>();
    }
}
