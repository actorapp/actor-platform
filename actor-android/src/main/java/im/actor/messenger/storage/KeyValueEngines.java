package im.actor.messenger.storage;

import com.droidkit.engine.keyvalue.DataAdapter;
import com.droidkit.engine.keyvalue.KeyValueEngine;
import com.droidkit.engine.keyvalue.StorageAdapter;
import com.droidkit.engine.keyvalue.sqlite.SQLiteStorageAdapter;

import com.droidkit.mvvm.CollectionBoxer;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.adapters.UsersAdapter;
import im.actor.model.entity.User;

public final class KeyValueEngines {

    private KeyValueEngines() {
    }

    private static final EngineHolder<User> USERS = new EngineHolder<User>(
            "USERS", new UsersAdapter());

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
                            SQLiteProvider.db(), databaseName, adapter);

                    engine = new KeyValueEngine<T>(storageAdapter, adapter);
                }
            }
            return engine;
        }
    }

}

