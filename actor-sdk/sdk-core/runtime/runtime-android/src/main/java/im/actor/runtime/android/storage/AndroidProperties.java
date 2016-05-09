/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import im.actor.runtime.storage.PreferencesStorage;

public class AndroidProperties implements PreferencesStorage {

    private final Context context;
    private boolean isLoaded = false;
    private SharedPreferences preference;

    public AndroidProperties(Context context) {
        this.context = context;
        load();
    }

    private void load() {
        if (!isLoaded) {
            isLoaded = true;
            this.preference = context.getSharedPreferences("properties.ini", Context.MODE_PRIVATE);
        }
    }

    @Override
    public synchronized void putLong(String key, long v) {
        load();
        preference.edit().putLong(key, v).commit();
    }

    @Override
    public synchronized long getLong(String key, long def) {
        load();
        return preference.getLong(key, def);
    }

    @Override
    public synchronized void putInt(String key, int v) {
        load();
        preference.edit().putInt(key, v).commit();
    }

    @Override
    public synchronized int getInt(String key, int def) {
        load();
        return preference.getInt(key, def);
    }

    @Override
    public synchronized void putBool(String key, boolean v) {
        load();
        preference.edit().putBoolean(key, v).commit();
    }

    @Override
    public synchronized boolean getBool(String key, boolean def) {
        load();
        return preference.getBoolean(key, def);
    }

    @Override
    public synchronized void putBytes(String key, byte[] v) {
        load();
        if (v != null) {
            preference.edit().putString(key, Base64.encodeToString(v, Base64.DEFAULT)).commit();
        } else {
            preference.edit().remove(key).commit();
        }
    }

    @Override
    public synchronized byte[] getBytes(String key) {
        load();
        String v = preference.getString(key, null);
        if (v != null) {
            return Base64.decode(v, Base64.DEFAULT);
        } else {
            return null;
        }

    }

    @Override
    public synchronized void putString(String key, String v) {
        load();
        preference.edit().putString(key, v).commit();
    }

    @Override
    public synchronized String getString(String key) {
        load();
        return preference.getString(key, null);
    }

    public synchronized void clear() {
        load();
        preference.edit().clear().commit();
    }
}
