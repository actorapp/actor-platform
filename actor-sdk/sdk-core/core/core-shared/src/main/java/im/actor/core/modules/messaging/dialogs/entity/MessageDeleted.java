package im.actor.core.modules.messaging.dialogs.entity;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class MessageDeleted implements AskMessage<Void> {

    private Peer peer;
    private Message topMessage;

    public MessageDeleted(Peer peer, Message topMessage) {
        this.peer = peer;
        this.topMessage = topMessage;
    }

    public Peer getPeer() {
        return peer;
    }

    public Message getTopMessage() {
        return topMessage;
    }
}
