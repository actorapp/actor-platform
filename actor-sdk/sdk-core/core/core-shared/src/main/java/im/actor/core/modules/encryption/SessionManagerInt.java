package im.actor.core.modules.encryption;

import im.actor.core.entity.encryption.PeerSession;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;

public class SessionManagerInt extends ActorInterface {

    public SessionManagerInt(ActorRef dest) {
        super(dest);
    }

    public Promise<PeerSession> pickSession(int uid, int keyGroup) {
        return ask(new SessionManagerActor.PickSessionForEncrypt(uid, keyGroup))
                .map(new Function<SessionManagerActor.PickSessionResp, PeerSession>() {
                    @Override
                    public PeerSession apply(SessionManagerActor.PickSessionResp src) {
                        return src.getSession();
                    }
                });
    }

    public Promise<PeerSession> pickSession(int uid, int keyGroup, long ownKeyId, long theirKeyId) {
        return ask(new SessionManagerActor.PickSessionForDecrypt(uid, keyGroup, theirKeyId, ownKeyId))
                .map(new Function<SessionManagerActor.PickSessionResp, PeerSession>() {
                    @Override
                    public PeerSession apply(SessionManagerActor.PickSessionResp src) {
                        return src.getSession();
                    }
                });
    }
}
