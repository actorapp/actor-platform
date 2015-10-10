package im.actor.core.modules.events;

import im.actor.core.entity.Peer;
import im.actor.runtime.eventbus.Event;

public class PeerInfoClosed extends Event {

    public static final String EVENT = "peer_info_closed";

    private Peer peer;

    public PeerInfoClosed(Peer peer) {
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
