/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android;

import android.database.sqlite.SQLiteDatabase;

import im.actor.runtime.StorageRuntime;
import im.actor.runtime.android.storage.AndroidProperties;
import im.actor.runtime.android.storage.NoOpOpenHelper;
import im.actor.runtime.android.storage.SQLiteIndexStorage;
import im.actor.runtime.android.storage.SQLiteKeyValue;
import im.actor.runtime.android.storage.SQLiteList;
import im.actor.runtime.storage.IndexStorage;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListStorage;
import im.actor.runtime.storage.PreferencesStorage;

public class AndroidStorageProvider implements StorageRuntime {

    private static final String DB = "ACTOR";

    private SQLiteDatabase database;
    private AndroidProperties properties;

    public AndroidStorageProvider() {
        this.properties = new AndroidProperties(AndroidContext.getContext());
    }

    @Override
    public PreferencesStorage createPreferencesStorage() {
        return properties;
    }

    @Override
    public IndexStorage createIndex(String name) {
        return new SQLiteIndexStorage(getDatabase(), "i_" + name);
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

    private synchronized SQLiteDatabase getDatabase() {
        if (database == null) {
            NoOpOpenHelper helper = new NoOpOpenHelper(AndroidContext.getContext(), DB);
            database = helper.getWritableDatabase();
        }
        return database;
    }
}
