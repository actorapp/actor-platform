/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network;

import com.google.j2objc.annotations.ObjectiveCName;

public interface AuthKeyStorage {
    @ObjectiveCName("getAuthKey")
    long getAuthKey();

    @ObjectiveCName("saveAuthKey:")
    void saveAuthKey(long key);
}
