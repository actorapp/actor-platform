package im.actor.model.storage;

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
        return new KeyValueEngine<User>() {

            private HashMap<Integer, User> users = new HashMap<Integer, User>();

            @Override
            public synchronized void addOrUpdateItem(User item) {
                users.put(item.getUid(), item);
            }

            @Override
            public synchronized void addOrUpdateItems(List<User> values) {
                for (User u : values) {
                    users.put(u.getUid(), u);
                }
            }

            @Override
            public synchronized void removeItem(long id) {
                users.remove((int) id);
            }

            @Override
            public synchronized void removeItems(long[] ids) {
                for (long id : ids) {
                    users.remove((int) id);
                }
            }

            @Override
            public synchronized void clear() {
                users.clear();
            }

            @Override
            public synchronized User getValue(long id) {
                return users.get((int) id);
            }
        };
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
