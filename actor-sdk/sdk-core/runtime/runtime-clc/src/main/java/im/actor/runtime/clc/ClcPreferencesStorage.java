package im.actor.runtime.clc;

import im.actor.runtime.storage.PreferencesStorage;

/**
 * Created by amir on 4/2/16.
 */
public interface ClcPreferencesStorage extends PreferencesStorage {
    void setContext(String context);
    String getContext();
}
