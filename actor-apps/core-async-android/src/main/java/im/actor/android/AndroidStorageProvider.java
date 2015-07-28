/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import im.actor.android.sql.NoOpOpenHelper;
import im.actor.android.sql.SQLiteKeyValue;
import im.actor.android.sql.SQLiteList;
import im.actor.model.droidkit.engine.IndexStorage;
import im.actor.model.droidkit.engine.KeyValueStorage;
import im.actor.model.droidkit.engine.ListStorage;
import im.actor.model.droidkit.engine.PreferencesStorage;
import im.actor.model.storage.BaseAsyncStorageProvider;

public class AndroidStorageProvider extends BaseAsyncStorageProvider {

    private static final String DB = "ACTOR";

    private Context context;
    private SQLiteDatabase database;
    private AndroidProperties properties;

    public AndroidStorageProvider(Context context) {
        this.context = context;
        this.properties = new AndroidProperties(context);
    }

    @Override
    public PreferencesStorage createPreferencesStorage() {
        return properties;
    }

    @Override
    public IndexStorage createIndex(String name) {
        return null;
    }

    private synchronized SQLiteDatabase getDatabase() {
        if (database == null) {
            NoOpOpenHelper helper = new NoOpOpenHelper(context, DB);
            database = helper.getWritableDatabase();
        }
        return database;
    }

    @Override
    public KeyValueStorage createKeyValue(String name) {
        return new SQLiteKeyValue(getDatabase(), "kv_" + name);
    }

    @Override
    public ListStorage createList(String name) {
        return new SQLiteList(getDatabase(), "ls_" + name);
    }

    @Override
    public void resetStorage() {
        properties.clear();
        database.rawQuery("select 'drop table ' || name || ';' from sqlite_master where type = 'table';", null);
    }
}
