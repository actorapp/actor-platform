/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.utils;

import im.actor.model.network.AuthKeyStorage;
import im.actor.model.droidkit.engine.PreferencesStorage;

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
