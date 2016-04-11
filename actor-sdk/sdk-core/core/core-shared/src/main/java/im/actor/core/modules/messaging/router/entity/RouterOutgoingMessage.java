package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;

public class RouterOutgoingMessage implements RouterMessageOnlyActive {

    private Peer peer;
    private Message message;

    public RouterOutgoingMessage(Peer peer, Message message) {
        this.peer = peer;
        this.message = message;
    }

    public Peer getPeer() {
        return peer;
    }

    public Message getMessage() {
        return message;
    }
}
