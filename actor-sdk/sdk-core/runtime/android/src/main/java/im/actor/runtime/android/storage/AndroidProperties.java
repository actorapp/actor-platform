/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import im.actor.runtime.storage.PreferencesStorage;

public class AndroidProperties implements PreferencesStorage {

    private SharedPreferences preference;

    public AndroidProperties(Context context) {
        this.preference = context.getSharedPreferences("properties.ini", Context.MODE_PRIVATE);
    }

    @Override
    public synchronized void putLong(String key, long v) {
        preference.edit().putLong(key, v).commit();
    }

    @Override
    public synchronized long getLong(String key, long def) {
        return preference.getLong(key, def);
    }

    @Override
    public synchronized void putInt(String key, int v) {
        preference.edit().putInt(key, v).commit();
    }

    @Override
    public synchronized int getInt(String key, int def) {
        return preference.getInt(key, def);
    }

    @Override
    public synchronized void putBool(String key, boolean v) {
        preference.edit().putBoolean(key, v).commit();
    }

    @Override
    public synchronized boolean getBool(String key, boolean def) {
        return preference.getBoolean(key, def);
    }

    @Override
    public synchronized void putBytes(String key, byte[] v) {
        preference.edit().putString(key, Base64.encodeToString(v, Base64.DEFAULT)).commit();
    }

    @Override
    public synchronized byte[] getBytes(String key) {
        String v = preference.getString(key, null);
        if (v != null) {
            return Base64.decode(v, Base64.DEFAULT);
        } else {
            return null;
        }

    }

    @Override
    public synchronized void putString(String key, String v) {
        preference.edit().putString(key, v).commit();
    }

    @Override
    public synchronized String getString(String key) {
        return preference.getString(key, null);
    }

    public synchronized void clear() {
        preference.edit().clear().commit();
    }
}
