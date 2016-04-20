package im.actor.core.modules.messaging.router.entity;

import java.util.List;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;

public class RouterNewMessages implements RouterMessageOnlyActive {

    private Peer peer;
    private List<Message> messages;
    private boolean isLastInDifference;

    public RouterNewMessages(Peer peer, List<Message> messages, boolean isLastInDifference) {
        this.peer = peer;
        this.messages = messages;
        this.isLastInDifference = isLastInDifference;
    }

    public Peer getPeer() {
        return peer;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public boolean isLastInDifference() {
        return isLastInDifference;
    }
}
