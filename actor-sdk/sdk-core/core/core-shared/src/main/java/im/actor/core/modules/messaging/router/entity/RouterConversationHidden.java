package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;

public class RouterConversationHidden {

    private final Peer peer;

    public RouterConversationHidden(Peer peer) {
        this.peer = peer;
    }

    public Peer getPeer() {
        return peer;
    }
}
