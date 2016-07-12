package im.actor.core.modules.encryption.ratchet;

import org.jetbrains.annotations.NotNull;

import im.actor.core.modules.encryption.entity.EncryptedBox;
import im.actor.core.modules.encryption.entity.UserKeys;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.promise.Promise;

/**
 * Encrypting data for private Secret Chats
 */
public class EncryptedUser extends ActorInterface {

    public EncryptedUser(@NotNull ActorRef dest) {
        super(dest);
    }

    /**
     * Encrypting data
     *
     * @param data data for encryption
     * @return promise of encrypted box
     */
    public Promise<EncryptedBox> encrypt(byte[] data) {
        return ask(new EncryptedUserActor.EncryptBox(data));
    }

    /**
     * Decrypting data
     *
     * @param data data for decryption
     * @return promise of decrypted box
     */
    public Promise<byte[]> decrypt(EncryptedBox data) {
        return ask(new EncryptedUserActor.DecryptBox(data));
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
