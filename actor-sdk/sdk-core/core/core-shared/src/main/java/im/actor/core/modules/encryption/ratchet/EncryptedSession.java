package im.actor.core.modules.encryption.ratchet;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.promise.Promise;

/**
 * Double Ratchet encrypted session operations
 */
public class EncryptedSession extends ActorInterface {

    public EncryptedSession(@NotNull ActorRef dest) {
        super(dest);
    }

    /**
     * Encrypt data for session
     *
     * @param data for encryption
     * @return promise of encrypted package
     */
    public Promise<EncryptedSessionActor.EncryptedPackageRes> encrypt(byte[] data) {
        return ask(new EncryptedSessionActor.EncryptPackage(data));
    }
}
