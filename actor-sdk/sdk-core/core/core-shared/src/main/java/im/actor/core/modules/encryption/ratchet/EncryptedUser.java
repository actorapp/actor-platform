package im.actor.core.modules.encryption.ratchet;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import im.actor.core.api.ApiEncyptedBoxKey;
import im.actor.core.modules.encryption.ratchet.entity.EncryptedUserKeys;
import im.actor.core.modules.encryption.ratchet.entity.UserKeys;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.promise.Promise;

/**
 * Encrypting shared key for private Secret Chats
 */
public class EncryptedUser extends ActorInterface {

    public EncryptedUser(@NotNull ActorRef dest) {
        super(dest);
    }

    /**
     * Encrypting shared key
     *
     * @param data shared key for encryption
     * @return promise of list of encrypted shared key
     */
    public Promise<EncryptedUserKeys> encrypt(byte[] data) {
        return ask(new EncryptedUserActor.EncryptBox(data));
    }

    /**
     * Decrypting shared key
     *
     * @param senderKeyGroupId sender's key group id
     * @param keys             list of encrypted box keys
     * @return promise of shared key
     */
    public Promise<byte[]> decrypt(int senderKeyGroupId, List<ApiEncyptedBoxKey> keys) {
        return ask(new EncryptedUserActor.DecryptBox(senderKeyGroupId, keys));
    }

    /**
     * Notify about user keys updated for refreshing internal keys cache
     *
     * @param updatedUserKeys updated user keys
     */
    public void onUserKeysChanged(UserKeys updatedUserKeys) {
        send(new EncryptedUserActor.KeyGroupUpdated(updatedUserKeys));
    }
}
