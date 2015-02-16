package im.actor.model.jvm;

import im.actor.model.network.AuthKeyStorage;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class MemoryAuthIdStorage implements AuthKeyStorage {

    private long key;

    @Override
    public long getAuthKey() {
        return key;
    }

    @Override
    public void saveAuthKey(long key) {
        this.key = key;
    }
}
