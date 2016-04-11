package im.actor.core.modules.messaging.router.entity;

import java.util.List;

import im.actor.core.entity.Peer;
import im.actor.core.entity.Reaction;

public class RouterChangedReactions implements RouterMessageOnlyActive {
    private Peer peer;
    private long rid;
    private List<Reaction> reactions;

    public RouterChangedReactions(Peer peer, long rid, List<Reaction> reactions) {
        this.peer = peer;
        this.rid = rid;
        this.reactions = reactions;
    }

    public Peer getPeer() {
        return peer;
    }

    public long getRid() {
        return rid;
    }

    public List<Reaction> getReactions() {
        return reactions;
    }
}
