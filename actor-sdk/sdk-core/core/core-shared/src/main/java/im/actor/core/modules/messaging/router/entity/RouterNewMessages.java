package im.actor.core.modules.messaging.router.entity;

import java.util.List;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;

public class RouterNewMessages implements RouterMessageOnlyActive {

    private Peer peer;
    private List<Message> messages;

    public RouterNewMessages(Peer peer, List<Message> messages) {
        this.peer = peer;
        this.messages = messages;
    }

    public Peer getPeer() {
        return peer;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
