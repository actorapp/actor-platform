package im.actor.core.modules.messaging.dialogs.entity;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class InMessage implements AskMessage<Void> {

    private Peer peer;
    private Message message;
    private int counter;

    public InMessage(Peer peer, Message message, int counter) {
        this.peer = peer;
        this.message = message;
        this.counter = counter;
    }

    public Peer getPeer() {
        return peer;
    }

    public Message getMessage() {
        return message;
    }

    public int getCounter() {
        return counter;
    }
}
