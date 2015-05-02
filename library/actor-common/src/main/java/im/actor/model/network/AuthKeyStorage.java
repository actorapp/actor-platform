/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network;

public interface AuthKeyStorage {
    long getAuthKey();

    void saveAuthKey(long key);
}
