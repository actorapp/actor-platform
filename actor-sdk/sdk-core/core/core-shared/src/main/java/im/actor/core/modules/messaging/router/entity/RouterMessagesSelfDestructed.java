package im.actor.core.modules.messaging.router.entity;

import java.util.List;

import im.actor.core.entity.Peer;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterMessagesSelfDestructed implements AskMessage<Void>, RouterMessageOnlyActive {
    private final Peer peer;
    private final List<Long> rids;

    public RouterMessagesSelfDestructed(Peer peer, List<Long> rids) {
        this.peer = peer;
        this.rids = rids;
    }

    public Peer getPeer() {
        return peer;
    }

    public List<Long> getRids() {
        return rids;
    }
}
