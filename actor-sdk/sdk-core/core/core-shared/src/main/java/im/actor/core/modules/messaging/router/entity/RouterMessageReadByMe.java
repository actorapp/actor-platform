package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;

public class RouterMessageReadByMe implements RouterMessageOnlyActive {

    private Peer peer;
    private long date;
    private int counter;

    public RouterMessageReadByMe(Peer peer, long date, int counter) {
        this.peer = peer;
        this.date = date;
        this.counter = counter;
    }

    public Peer getPeer() {
        return peer;
    }

    public long getDate() {
        return date;
    }

    public int getCounter() {
        return counter;
    }
}
