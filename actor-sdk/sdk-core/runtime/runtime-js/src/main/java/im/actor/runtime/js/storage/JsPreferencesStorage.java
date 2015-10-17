/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.storage;

import com.google.gwt.storage.client.Storage;

import im.actor.runtime.storage.PreferencesStorage;

import static im.actor.runtime.crypto.Base64Utils.fromBase64;
import static im.actor.runtime.crypto.Base64Utils.toBase64;

public class JsPreferencesStorage implements PreferencesStorage {
    private Storage storage;

    public JsPreferencesStorage(Storage storage) {
        this.storage = storage;
    }

    private String convertKey(String key) {
        return "prefs_" + key;
    }

    @Override
    public void putLong(String key, long v) {
        storage.setItem(convertKey(key), "" + v);
    }

    @Override
    public long getLong(String key, long def) {
        String v = storage.getItem(convertKey(key));
        if (v != null) {
            return Long.parseLong(v);
        } else {
            return def;
        }
    }

    @Override
    public void putInt(String key, int v) {
        storage.setItem(convertKey(key), "" + v);
    }

    @Override
    public int getInt(String key, int def) {
        String v = storage.getItem(convertKey(key));
        if (v != null) {
            return Integer.parseInt(v);
        } else {
            return def;
        }
    }

    @Override
    public void putBool(String key, boolean v) {
        storage.setItem(convertKey(key), "" + v);
    }

    @Override
    public boolean getBool(String key, boolean def) {
        String v = storage.getItem(convertKey(key));
        if (v != null) {
            return Boolean.parseBoolean(v);
        } else {
            return def;
        }
    }

    @Override
    public void putBytes(String key, byte[] v) {
        storage.setItem(convertKey(key), toBase64(v));
    }

    @Override
    public byte[] getBytes(String key) {
        String v = storage.getItem(convertKey(key));
        if (v != null) {
            return fromBase64(v);
        } else {
            return null;
        }
    }

    @Override
    public void putString(String key, String v) {
        storage.setItem(convertKey(key), v);
    }

    @Override
    public String getString(String key) {
        return storage.getItem(convertKey(key));
    }
}
