package im.actor.model.storage;

import im.actor.model.MemoryKeyValueEngine;
import im.actor.model.entity.*;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class MemoryEnginesFactory implements EnginesFactory {

    @Override
    public KeyValueEngine<User> createUsersEngine() {
        return new MemoryKeyValueEngine<User>();
    }

    @Override
    public ListEngine<Dialog> createDialogsEngine() {
        return new MemoryListEngine<Dialog>();
    }

    @Override
    public ListEngine<Message> createMessagesEngine(Peer peer) {
        return new MemoryListEngine<Message>();
    }

    @Override
    public KeyValueEngine<PendingMessage> pendingMessages(Peer peer) {
        return null;
    }

}
