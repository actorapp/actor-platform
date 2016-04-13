package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;

public class RouterConversationVisible {

    private final Peer peer;

    public RouterConversationVisible(Peer peer) {
        this.peer = peer;
    }

    public Peer getPeer() {
        return peer;
    }
}
