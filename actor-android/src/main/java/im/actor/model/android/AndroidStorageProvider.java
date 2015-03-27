package im.actor.model.android;

import im.actor.model.android.sql.SQLiteKeyValue;
import im.actor.model.android.sql.SQLiteList;
import im.actor.model.android.sql.SQLiteProvider;
import im.actor.model.droidkit.engine.KeyValueStorage;
import im.actor.model.droidkit.engine.ListStorage;
import im.actor.model.droidkit.engine.PreferencesStorage;
import im.actor.model.storage.BaseAsyncStorageProvider;

/**
 * Created by ex3ndr on 14.03.15.
 */
public class AndroidStorageProvider extends BaseAsyncStorageProvider {

    @Override
    public PreferencesStorage createPreferencesStorage() {
        return new AndroidProperties();
    }

    @Override
    public KeyValueStorage createKeyValue(String name) {
        return new SQLiteKeyValue(SQLiteProvider.db(), "kv_" + name);
    }

    @Override
    public ListStorage createList(String name) {
        return new SQLiteList(SQLiteProvider.db(), "ls_" + name);
    }
}
