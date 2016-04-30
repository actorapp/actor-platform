package im.actor.core.modules.messaging.dialogs.entity;

import im.actor.core.entity.Peer;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class ChatDelete implements AskMessage<Void> {

    private Peer peer;

    public ChatDelete(Peer peer) {
        this.peer = peer;
    }

    public Peer getPeer() {
        return peer;
    }
}
