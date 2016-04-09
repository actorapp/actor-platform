package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;

public class RouterOutgoingError {

    private Peer peer;
    private long rid;

    public RouterOutgoingError(Peer peer, long rid) {
        this.peer = peer;
        this.rid = rid;
    }

    public Peer getPeer() {
        return peer;
    }

    public long getRid() {
        return rid;
    }
}
