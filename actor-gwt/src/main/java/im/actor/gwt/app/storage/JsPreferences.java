package im.actor.gwt.app.storage;

import com.google.gwt.storage.client.Storage;

import im.actor.gwt.app.base64.Base64Utils;
import im.actor.model.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 10.02.15.
 */
public class JsPreferences implements PreferencesStorage {
    private Storage storage;

    public JsPreferences(Storage storage) {
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
        storage.setItem(convertKey(key), Base64Utils.toBase64(v));
    }

    @Override
    public byte[] getBytes(String key) {
        String v = storage.getItem(convertKey(key));
        if (v != null) {
            return Base64Utils.fromBase64(v);
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
