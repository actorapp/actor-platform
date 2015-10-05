package im.actor.core.modules.events;

import im.actor.core.entity.Peer;
import im.actor.runtime.eventbus.Event;

public class PeerChatClosed extends Event {

    public static final String EVENT = "peer_chat_closed";

    private Peer peer;

    public PeerChatClosed(Peer peer) {
        this.peer = peer;
    }

    public Peer getPeer() {
        return peer;
    }

    @Override
    public String getType() {
        return EVENT;
    }

    @Override
    public String toString() {
        return EVENT + " {" + peer.toIdString() + "}";
    }
}
