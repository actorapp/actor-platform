package im.actor.core.modules.encryption.ratchet;

import org.jetbrains.annotations.NotNull;

import im.actor.core.api.ApiEncryptedMessage;
import im.actor.core.api.ApiMessage;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.promise.Promise;

/**
 * Entry point for message encryption
 */
public class EncryptedMsg extends ActorInterface {

    public EncryptedMsg(@NotNull ActorRef dest) {
        super(dest);
    }

    /**
     * Encrypt Message for private secret chat
     *
     * @param uid     user's id
     * @param message message content
     * @return promise of encrypted message
     */
    public Promise<ApiEncryptedMessage> encrypt(int uid, ApiMessage message) {
        return ask(new EncryptedMsgActor.EncryptMessage(uid, message));
    }
}
