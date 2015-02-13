package im.actor.model.storage;

import im.actor.model.entity.*;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface EnginesFactory {
    public KeyValueEngine<User> createUsersEngine();

    public ListEngine<Dialog> createDialogsEngine();

    public ListEngine<Message> createMessagesEngine(Peer peer);

    public KeyValueEngine<PendingMessage> pendingMessages(Peer peer);
}