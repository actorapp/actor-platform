package im.actor.core.events;

import im.actor.core.entity.Peer;
import im.actor.runtime.eventbus.Event;

public class PeerChatPreload extends Event {

    public static final String EVENT = "peer_chat_preload";

    private Peer peer;

    public PeerChatPreload(Peer peer) {
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
