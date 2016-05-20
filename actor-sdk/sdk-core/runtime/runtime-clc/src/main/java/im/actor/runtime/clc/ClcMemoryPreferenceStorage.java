package im.actor.runtime.clc;

import im.actor.runtime.storage.PreferencesStorage;
import im.actor.runtime.storage.memory.MemoryPreferencesStorage;

import java.util.prefs.Preferences;

/**
 * Created by mohammad on 11/23/15.
 */
public class ClcMemoryPreferenceStorage implements PreferencesStorage {

    MemoryPreferencesStorage memStoragePref = new MemoryPreferencesStorage();

    @Override
    public void putLong(String key, long v) {
        memStoragePref.putLong(key,v);
    }

    @Override
    public long getLong(String key, long def) {
        return 0;
    }

    @Override
    public void putInt(String key, int v) {

    }

    @Override
    public int getInt(String key, int def) {
        return 0;
    }

    @Override
    public void putBool(String key, boolean v) {

    }

    @Override
    public boolean getBool(String key, boolean def) {
        return false;
    }

    @Override
    public void putBytes(String key, byte[] v) {

    }

    @Override
    public byte[] getBytes(String key) {
        return new byte[0];
    }

    @Override
    public void putString(String key, String v) {

    }

    @Override
    public String getString(String key) {
        return null;
    }
}
