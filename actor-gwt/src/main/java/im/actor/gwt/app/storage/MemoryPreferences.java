package im.actor.gwt.app.storage;

import java.util.HashMap;

import im.actor.model.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 21.02.15.
 */
public class MemoryPreferences implements PreferencesStorage {

    private HashMap<String, Object> map = new HashMap<String, Object>();

    @Override
    public void putLong(String key, long v) {
        map.put(key, v);
    }

    @Override
    public long getLong(String key, long def) {
        if (map.containsKey(key)) {
            return (Long) map.get(key);
        }
        return def;
    }

    @Override
    public void putInt(String key, int v) {
        map.put(key, v);
    }

    @Override
    public int getInt(String key, int def) {
        if (map.containsKey(key)) {
            return (Integer) map.get(key);
        }
        return def;
    }

    @Override
    public void putBool(String key, boolean v) {
        map.put(key, v);
    }

    @Override
    public boolean getBool(String key, boolean def) {
        if (map.containsKey(key)) {
            return (Boolean) map.get(key);
        }
        return def;
    }

    @Override
    public void putBytes(String key, byte[] v) {
        map.put(key, v);
    }

    @Override
    public byte[] getBytes(String key) {
        if (map.containsKey(key)) {
            return (byte[]) map.get(key);
        }
        return null;
    }

    @Override
    public void putString(String key, String v) {
        map.put(key, v);
    }

    @Override
    public String getString(String key) {
        if (map.containsKey(key)) {
            return (String) map.get(key);
        }
        return null;
    }
}
