package im.actor.core.modules.messaging.router.entity;

import java.util.List;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;

public class RouterApplyChatHistory {

    private Peer peer;
    private List<Message> messages;
    private Long maxReceiveDate;
    private Long maxReadDate;

    public RouterApplyChatHistory(Peer peer, List<Message> messages, Long maxReceiveDate, Long maxReadDate) {
        this.peer = peer;
        this.messages = messages;
        this.maxReceiveDate = maxReceiveDate;
        this.maxReadDate = maxReadDate;
    }

    public Peer getPeer() {
        return peer;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Long getMaxReceiveDate() {
        return maxReceiveDate;
    }

    public Long getMaxReadDate() {
        return maxReadDate;
    }
}
