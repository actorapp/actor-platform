package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;

public class RouterMessageReceived {

    private Peer peer;
    private long date;

    public RouterMessageReceived(Peer peer, long date) {
        this.peer = peer;
        this.date = date;
    }

    public Peer getPeer() {
        return peer;
    }

    public long getDate() {
        return date;
    }
}
