package im.actor.core.modules.encryption.ratchet;

import im.actor.core.api.ApiEncyptedBoxKey;
import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.modules.ModuleContext;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

/**
 * Double Ratchet encrypted session operations
 */
public class EncryptedSession extends ActorInterface {

    private PeerSession session;

    /**
     * Constructor of session encryption
     *
     * @param session session settings
     * @param context context
     */
    public EncryptedSession(PeerSession session, ModuleContext context) {
        super(system().actorOf("encryption/uid_" + session.getUid() + "/session_" +
                RandomUtils.nextRid(), "encrypt", () -> new EncryptedSessionActor(context, session)));
        this.session = session;
    }

    /**
     * Get Peer Session parameters
     *
     * @return peer session
     */
    public PeerSession getSession() {
        return session;
    }

    /**
     * Encrypt data for session
     *
     * @param data for encryption
     * @return promise of encrypted package
     */
    public Promise<ApiEncyptedBoxKey> encrypt(byte[] data) {
        return ask(new EncryptedSessionActor.EncryptPackage(data));
    }

    /**
     * Decrypt data for session
     *
     * @param key for decryption
     * @return promise of decrypted package
     */
    public Promise<byte[]> decrypt(ApiEncyptedBoxKey key) {
        return ask(new EncryptedSessionActor.DecryptPackage(key));
    }
}
