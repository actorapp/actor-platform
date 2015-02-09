package im.actor.model.storage;

import java.util.HashMap;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class MemoryPreferences implements PreferencesStorage {

    private HashMap<String, Object> items = new HashMap<String, Object>();

    @Override
    public synchronized void putLong(String key, long v) {
        items.put(key, v);
    }

    @Override
    public synchronized long getLong(String key, long def) {
        if (items.containsKey(key)) {
            return (Long) items.get(key);
        }
        return def;
    }

    @Override
    public void putInt(String key, int v) {
        items.put(key, v);
    }

    @Override
    public int getInt(String key, int def) {
        if (items.containsKey(key)) {
            return (Integer) items.get(key);
        }
        return def;
    }

    @Override
    public void putBool(String key, boolean v) {
        items.put(key, v);
    }

    @Override
    public boolean getBool(String key, boolean def) {
        if (items.containsKey(key)) {
            return (Boolean) items.get(key);
        }
        return def;
    }

    @Override
    public void putBytes(String key, byte[] v) {
        items.put(key, v);
    }

    @Override
    public byte[] getBytes(String key) {
        if (items.containsKey(key)) {
            return (byte[]) items.get(key);
        }
        return null;
    }

    @Override
    public void putString(String key, String v) {
        items.put(key, v);
    }

    @Override
    public String getString(String key) {
        if (items.containsKey(key)) {
            return (String) items.get(key);
        }
        return null;
    }
}
