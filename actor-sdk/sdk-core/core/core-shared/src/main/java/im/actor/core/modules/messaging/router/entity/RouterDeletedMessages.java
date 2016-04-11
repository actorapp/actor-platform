package im.actor.core.modules.messaging.router.entity;

import java.util.List;

import im.actor.core.entity.Peer;

public class RouterDeletedMessages implements RouterMessageOnlyActive {

    private Peer peer;
    private List<Long> rids;

    public RouterDeletedMessages(Peer peer, List<Long> rids) {
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
