package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;

public class RouterChatDelete implements RouterMessageOnlyActive {

    private Peer peer;

    public RouterChatDelete(Peer peer) {
        this.peer = peer;
    }

    public Peer getPeer() {
        return peer;
    }
}
