/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

public interface PreferencesStorage {

    void putLong(String key, long v);

    long getLong(String key, long def);

    void putInt(String key, int v);

    int getInt(String key, int def);

    void putBool(String key, boolean v);

    boolean getBool(String key, boolean def);

    void putBytes(String key, byte[] v);

    byte[] getBytes(String key);

    void putString(String key, String v);

    String getString(String key);
}