package im.actor.core.modules.encryption.ratchet;

import im.actor.core.entity.encryption.PeerSession;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

/**
 * Session Manager for encrypted chats.
 * Stores and manages encrypted sessions between users.
 * Can be asked to pick session parameters for specific users.
 */
public class SessionManager extends ActorInterface {

    public SessionManager(ModuleContext context) {
        super(system().actorOf("encryption/sessions", () -> new SessionManagerActor(context)));
    }

    /**
     * Pick fresh session with random pre keys
     *
     * @param uid      user's id
     * @param keyGroup key group id
     * @return promise of session
     */
    public Promise<PeerSession> pickSession(int uid, int keyGroup) {
        return ask(new SessionManagerActor.PickSessionForEncrypt(uid, keyGroup));
    }

    /**
     * Pick session with specific identity keys
     *
     * @param uid        user's id
     * @param keyGroup   key group id
     * @param ownKeyId   own identity prekey id
     * @param theirKeyId their identity prekey id
     * @return promise of session
     */
    public Promise<PeerSession> pickSession(int uid, int keyGroup, long ownKeyId, long theirKeyId) {
        return ask(new SessionManagerActor.PickSessionForDecrypt(uid, keyGroup, theirKeyId, ownKeyId));
    }
}
