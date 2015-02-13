package im.actor.messenger.api;

import android.content.Context;
import android.content.SharedPreferences;

import im.actor.api.ActorApiStorage;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class AndroidApiStorage implements ActorApiStorage {
    private SharedPreferences preferences;

    public AndroidApiStorage(Context context) {
        preferences = context.getSharedPreferences("SecretSharedPreferencesKey", Context.MODE_PRIVATE);
    }

    @Override
    public synchronized long getAuthKey() {
        return preferences.getLong("authId", 0);
    }

    @Override
    public synchronized void saveAuthKey(long key) {
        preferences.edit().putLong("authId", key).commit();
    }
}
