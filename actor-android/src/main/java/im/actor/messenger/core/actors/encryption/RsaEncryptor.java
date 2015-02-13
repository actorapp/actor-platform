package im.actor.messenger.core.actors.encryption;

import com.droidkit.actors.concurrency.Future;

import im.actor.messenger.storage.scheme.users.PublicKey;

/**
 * Created by ex3ndr on 14.09.14.
 */
public interface RsaEncryptor {
    public Future<RsaResult> encrypt(byte[] data, PublicKey[] myKeys, PublicKey[] foreign);
}
