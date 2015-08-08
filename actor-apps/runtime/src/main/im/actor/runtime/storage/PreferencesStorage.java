/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

public interface PreferencesStorage {

    @ObjectiveCName("putLongWithKey:withValue:")
    void putLong(String key, long v);

    @ObjectiveCName("getLongWithKey:withDefault:")
    long getLong(String key, long def);

    @ObjectiveCName("putIntWithKey:withValue:")
    void putInt(String key, int v);

    @ObjectiveCName("getIntWithKey:withDefault:")
    int getInt(String key, int def);

    @ObjectiveCName("putBoolWithKey:withValue:")
    void putBool(String key, boolean v);

    @ObjectiveCName("getBoolWithKey:withDefault:")
    boolean getBool(String key, boolean def);

    @ObjectiveCName("putBytesWithKey:withValue:")
    void putBytes(String key, byte[] v);

    @ObjectiveCName("getBytesWithKey:")
    byte[] getBytes(String key);

    @ObjectiveCName("putStringWithKey:withValue:")
    void putString(String key, String v);

    @ObjectiveCName("getStringWithKey:")
    String getString(String key);
}