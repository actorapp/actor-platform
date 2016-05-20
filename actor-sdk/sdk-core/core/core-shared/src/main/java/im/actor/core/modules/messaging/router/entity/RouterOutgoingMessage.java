package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterOutgoingMessage implements AskMessage<Void>, RouterMessageOnlyActive {

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
