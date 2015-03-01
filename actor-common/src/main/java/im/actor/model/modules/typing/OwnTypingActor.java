package im.actor.model.modules.typing;

import im.actor.model.api.OutPeer;
import im.actor.model.api.TypingType;
import im.actor.model.api.rpc.RequestTyping;
import im.actor.model.droidkit.actors.ActorTime;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.User;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;

/**
 * Created by ex3ndr on 15.02.15.
 */
public class OwnTypingActor extends ModuleActor {

    private static final long TYPING_DELAY = 1000L;

    private long lastTypingTime = 0;

    public OwnTypingActor(Modules messenger) {
        super(messenger);
    }

    private void onTyping(Peer peer) {
        if (ActorTime.currentTime() - lastTypingTime < TYPING_DELAY) {
            return;
        }
        lastTypingTime = ActorTime.currentTime();

        OutPeer outPeer;
        if (peer.getPeerType() == PeerType.PRIVATE) {
            User user = getUser(peer.getPeerId());
            if (user == null) {
                return;
            }
            outPeer = new OutPeer(im.actor.model.api.PeerType.PRIVATE, user.getUid(), user.getAccessHash());
        } else if (peer.getPeerType() == PeerType.GROUP) {
            // TODO: Implement for groups
            return;
        } else {
            return;
        }

        request(new RequestTyping(outPeer, TypingType.TEXT));
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Typing) {
            onTyping(((Typing) message).getPeer());
        } else {
            drop(message);
        }
    }

    public static class Typing {
        private Peer peer;

        public Typing(Peer peer) {
            this.peer = peer;
        }

        public Peer getPeer() {
            return peer;
        }
    }
}
