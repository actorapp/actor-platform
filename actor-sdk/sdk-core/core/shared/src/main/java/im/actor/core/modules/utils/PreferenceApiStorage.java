/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.utils;

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
    public void saveAuthKey(long key) {
        preferencesStorage.putLong("auth_id", key);
    }
}
