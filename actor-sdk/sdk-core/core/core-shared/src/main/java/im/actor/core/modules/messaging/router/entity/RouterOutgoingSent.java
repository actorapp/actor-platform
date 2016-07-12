package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterOutgoingSent implements AskMessage<Void>, RouterMessageOnlyActive {

    private Peer peer;
    private long rid;
    private long date;

    public RouterOutgoingSent(Peer peer, long rid, long date) {
        this.peer = peer;
        this.rid = rid;
        this.date = date;
    }

    public Peer getPeer() {
        return peer;
    }

    public long getRid() {
        return rid;
    }

    public long getDate() {
        return date;
    }
}
