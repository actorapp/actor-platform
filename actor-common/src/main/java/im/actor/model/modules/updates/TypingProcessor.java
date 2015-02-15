package im.actor.model.modules.updates;

import im.actor.model.Messenger;
import im.actor.model.api.Peer;
import im.actor.model.api.PeerType;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.modules.typing.TypingActor;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class TypingProcessor {
    private Messenger messenger;
    private ActorRef typingActor;

    public TypingProcessor(Messenger messenger) {
        this.messenger = messenger;
        this.typingActor = TypingActor.get(messenger);
    }

    public void onTyping(Peer peer, int uid, int type) {
        if (peer.getType() == PeerType.PRIVATE) {
            typingActor.sendOnce(new TypingActor.PrivateTyping(uid, type));
        } else if (peer.getType() == PeerType.GROUP) {
            typingActor.sendOnce(new TypingActor.GroupTyping(peer.getId(), uid, type));
        } else {
            // Ignore
        }
    }

    public void onMessage(Peer peer, int uid) {
        if (peer.getType() == PeerType.PRIVATE) {
            typingActor.sendOnce(new TypingActor.StopTyping(uid));
        } else if (peer.getType() == PeerType.GROUP) {
            typingActor.sendOnce(new TypingActor.StopGroupTyping(peer.getId(), uid));
        } else {
            // Ignore
        }
    }
}
