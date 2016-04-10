package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;

public class RouterChatClear {

    private Peer peer;

    public RouterChatClear(Peer peer) {
        this.peer = peer;
    }

    public Peer getPeer() {
        return peer;
    }
}
