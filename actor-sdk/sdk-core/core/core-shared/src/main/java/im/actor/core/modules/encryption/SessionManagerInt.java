package im.actor.core.modules.encryption;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.PeerSession;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class SessionManagerInt extends ActorInterface {

    public SessionManagerInt(ModuleContext context) {
        super(system().actorOf("encryption/sessions",
                SessionManagerActor.CONSTRUCTOR(context)));
    }

    public Promise<PeerSession> pickSession(int uid, int keyGroup) {
        return ask(new SessionManagerActor.PickSessionForEncrypt(uid, keyGroup));
    }

    public Promise<PeerSession> pickSession(int uid, int keyGroup, long ownKeyId, long theirKeyId) {
        return ask(new SessionManagerActor.PickSessionForDecrypt(uid, keyGroup, theirKeyId, ownKeyId));
    }
}
