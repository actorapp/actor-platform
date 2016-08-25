package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterSecretChatCleared implements AskMessage<Void>, RouterMessageOnlyActive {

    private Peer peer;

    public RouterSecretChatCleared(Peer peer) {
        this.peer = peer;
    }

    public Peer getPeer() {
        return peer;
    }
}
