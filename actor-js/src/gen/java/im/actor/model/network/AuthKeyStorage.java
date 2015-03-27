package im.actor.model.network;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface AuthKeyStorage {
    public long getAuthKey();

    public void saveAuthKey(long key);
}
