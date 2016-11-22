package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterResetChat implements AskMessage<Void> {
    
    private Peer peer;

    public RouterResetChat(Peer peer) {
        this.peer = peer;
    }

    public Peer getPeer() {
        return peer;
    }
}
