package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterOutgoingError implements AskMessage<Void>, RouterMessageOnlyActive {

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
