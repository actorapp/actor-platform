package im.actor.model.storage.temp;

import java.util.HashMap;

import im.actor.model.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class TempPreferences implements PreferencesStorage {

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
    public synchronized void putInt(String key, int v) {
        items.put(key, v);
    }

    @Override
    public synchronized int getInt(String key, int def) {
        if (items.containsKey(key)) {
            return (Integer) items.get(key);
        }
        return def;
    }

    @Override
    public synchronized void putBool(String key, boolean v) {
        items.put(key, v);
    }

    @Override
    public synchronized boolean getBool(String key, boolean def) {
        if (items.containsKey(key)) {
            return (Boolean) items.get(key);
        }
        return def;
    }

    @Override
    public synchronized void putBytes(String key, byte[] v) {
        items.put(key, v);
    }

    @Override
    public synchronized byte[] getBytes(String key) {
        if (items.containsKey(key)) {
            return (byte[]) items.get(key);
        }
        return null;
    }

    @Override
    public synchronized void putString(String key, String v) {
        items.put(key, v);
    }

    @Override
    public synchronized String getString(String key) {
        if (items.containsKey(key)) {
            return (String) items.get(key);
        }
        return null;
    }
}
