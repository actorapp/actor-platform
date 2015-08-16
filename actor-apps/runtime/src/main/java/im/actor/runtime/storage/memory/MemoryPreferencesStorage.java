package im.actor.runtime.storage.memory;

import java.util.HashMap;

import im.actor.runtime.storage.PreferencesStorage;

public class MemoryPreferencesStorage implements PreferencesStorage {

    private HashMap<String, Object> keys = new HashMap<String, Object>();

    @Override
    public void putLong(String key, long v) {
        keys.put(key, v);
    }

    @Override
    public long getLong(String key, long def) {
        if (keys.containsKey(key)) {
            return (Long) keys.get(key);
        }
        return def;
    }

    @Override
    public void putInt(String key, int v) {
        keys.put(key, v);
    }

    @Override
    public int getInt(String key, int def) {
        if (keys.containsKey(key)) {
            return (Integer) keys.get(key);
        }
        return def;
    }

    @Override
    public void putBool(String key, boolean v) {
        keys.put(key, v);
    }

    @Override
    public boolean getBool(String key, boolean def) {
        if (keys.containsKey(key)) {
            return (Boolean) keys.get(key);
        }
        return def;
    }

    @Override
    public void putBytes(String key, byte[] v) {
        keys.put(key, v);
    }

    @Override
    public byte[] getBytes(String key) {
        if (keys.containsKey(key)) {
            return (byte[]) keys.get(key);
        }
        return null;
    }

    @Override
    public void putString(String key, String v) {
        keys.put(key, v);
    }

    @Override
    public String getString(String key) {
        if (keys.containsKey(key)) {
            return (String) keys.get(key);
        }
        return null;
    }
}
