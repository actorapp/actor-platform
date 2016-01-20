package im.actor.core.events;

import im.actor.core.entity.Peer;
import im.actor.runtime.eventbus.Event;

public class PeerInfoOpened extends Event {

    public static final String EVENT = "peer_info_opened";

    private Peer peer;

    public PeerInfoOpened(Peer peer) {
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
