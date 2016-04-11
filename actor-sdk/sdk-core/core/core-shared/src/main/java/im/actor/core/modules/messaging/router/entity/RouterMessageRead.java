package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;

public class RouterMessageRead implements RouterMessageOnlyActive {

    private Peer peer;
    private long date;

    public RouterMessageRead(Peer peer, long date) {
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
