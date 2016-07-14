package im.actor.core.modules.encryption.ratchet;

import java.util.List;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiEncryptedContent;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.ratchet.entity.EncryptedMessage;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

/**
 * Entry point for message encryption
 */
public class EncryptedMsg extends ActorInterface {

    /**
     * Constructor of encrypted messaging interface
     *
     * @param context context
     */
    public EncryptedMsg(ModuleContext context) {
        super(system().actorOf("encryption/messaging", () -> new EncryptedMsgActor(context)));
    }

    /**
     * Encrypt Message for secret chats
     *
     * @param uids    User's ids. Add own UID for sending to other devices
     * @param message message content
     * @return promise of encrypted message
     */
    public Promise<EncryptedMessage> encrypt(List<Integer> uids, ApiEncryptedContent message) {
        return ask(new EncryptedMsgActor.EncryptMessage(message, uids));
    }

    /**
     * Decrypt Message from private secret chat
     *
     * @param uid          user's id
     * @param encryptedBox encrypted message
     * @return promise of decrypted message
     */
    public Promise<ApiEncryptedContent> decrypt(int uid, ApiEncryptedBox encryptedBox) {
        return ask(new EncryptedMsgActor.DecryptMessage(uid, encryptedBox));
    }
}
