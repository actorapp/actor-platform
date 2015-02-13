package im.actor.messenger.storage;

import com.droidkit.engine.keyvalue.DataAdapter;
import com.droidkit.engine.keyvalue.KeyValueEngine;
import com.droidkit.engine.keyvalue.StorageAdapter;
import com.droidkit.engine.keyvalue.sqlite.SQLiteStorageAdapter;
import com.droidkit.mvvm.CollectionBoxer;

import im.actor.messenger.core.AppContext;
import im.actor.messenger.model.GroupModel;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.adapters.KeyValueBserAdapter;
import im.actor.messenger.storage.scheme.groups.GroupInfo;
import im.actor.messenger.storage.scheme.media.Downloaded;
import im.actor.messenger.storage.scheme.messages.ReadState;
import im.actor.messenger.storage.scheme.users.PublicKey;
import im.actor.messenger.storage.scheme.users.User;

import java.io.IOException;

public final class KeyValueEngines {

    private KeyValueEngines() {
    }

    private static final EngineHolder<User> USERS = new EngineHolder<User>(
            "USERS", new KeyValueBserAdapter<User>(User.class));

    private static CollectionBoxer<Integer, User, UserModel> usersBoxer = new CollectionBoxer<Integer, User, UserModel>() {
        @Override
        protected UserModel wrap(Integer key, User raw) {
            return new UserModel(raw);
        }

        @Override
        protected User load(Integer key) {
            return USERS.getEngine().get(key);
        }

        @Override
        protected void save(Integer key, User raw) {
            USERS.getEngine().put(raw);
        }

        @Override
        protected void update(Integer key, User raw, UserModel wrap) {
            wrap.update(raw);
        }
    };

    public static CollectionBoxer<Integer, User, UserModel> users() {
        return usersBoxer;
    }


    private static final EngineHolder<GroupInfo> GROUPS = new EngineHolder<GroupInfo>(
            "GROUPS", new KeyValueBserAdapter<GroupInfo>(GroupInfo.class) {

        @Override
        public long getId(GroupInfo value) {
            return value.getGroupId();
        }
    });

    private static CollectionBoxer<Integer, GroupInfo, GroupModel> groupsBoxer = new CollectionBoxer<Integer, GroupInfo, GroupModel>() {
        @Override
        protected GroupModel wrap(Integer key, GroupInfo raw) {
            return new GroupModel(raw);
        }

        @Override
        protected GroupInfo load(Integer key) {
            return GROUPS.getEngine().get(key);
        }

        @Override
        protected void save(Integer key, GroupInfo raw) {
            GROUPS.getEngine().put(raw);
        }

        @Override
        protected void update(Integer key, GroupInfo raw, GroupModel wrap) {
            wrap.update(raw);
        }
    };

    public static CollectionBoxer<Integer, GroupInfo, GroupModel> groups() {
        return groupsBoxer;
    }


    private static final EngineHolder<PublicKey> PUBLIC_KEYS = new EngineHolder<PublicKey>(
            "PUBLIC_KEYS", new KeyValueBserAdapter<PublicKey>(PublicKey.class));

    public static KeyValueEngine<PublicKey> publicKeys() {
        return PUBLIC_KEYS.getEngine();
    }

    private static final EngineHolder<Downloaded> DOWNLOADED = new EngineHolder<Downloaded>("DOWNLOADED",
            new KeyValueBserAdapter<Downloaded>(Downloaded.class));

    public static KeyValueEngine<Downloaded> downloaded() {
        return DOWNLOADED.getEngine();
    }

    private static EngineHolder<ReadState> CONVERSATIONS = new EngineHolder<ReadState>("CONVERSATIONS",
            new KeyValueBserAdapter<ReadState>(ReadState.class));

    public static KeyValueEngine<ReadState> readStates() {
        return CONVERSATIONS.getEngine();
    }


    // Base

    private static class EngineHolder<T> {
        private final Object lock = new Object();
        private String databaseName;
        private DataAdapter<T> adapter;

        private volatile KeyValueEngine<T> engine;

        public EngineHolder(String databaseName, DataAdapter<T> adapter) {
            this.databaseName = databaseName;
            this.adapter = adapter;
        }

        public KeyValueEngine<T> getEngine() {
            if (engine == null) {
                synchronized (lock) {
                    StorageAdapter<T> storageAdapter = new SQLiteStorageAdapter<T>(
                            DbProvider.getDatabase(AppContext.getContext()),
                            databaseName,
                            adapter);

                    engine = new KeyValueEngine<T>(storageAdapter, adapter);
                }
            }
            return engine;
        }
    }

}

