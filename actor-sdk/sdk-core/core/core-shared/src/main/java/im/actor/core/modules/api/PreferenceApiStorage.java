/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.api;

import im.actor.core.network.AuthKeyStorage;
import im.actor.runtime.storage.PreferencesStorage;

public class PreferenceApiStorage implements AuthKeyStorage {

    private PreferencesStorage preferencesStorage;

    public PreferenceApiStorage(PreferencesStorage preferencesStorage) {
        this.preferencesStorage = preferencesStorage;
    }

    @Override
    public long getAuthKey() {
        return preferencesStorage.getLong("auth_id", 0);
    }

    @Override
    public byte[] getAuthMasterKey() {
        return preferencesStorage.getBytes("auth_master_key");
    }

    @Override
    public void saveAuthKey(long key) {
        preferencesStorage.putLong("auth_id", key);
    }

    @Override
    public void saveMasterKey(byte[] masterKey) {
        preferencesStorage.putBytes("auth_master_key", masterKey);
    }
}
