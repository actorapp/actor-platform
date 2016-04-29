package im.actor.core.modules.messaging.router.entity;

import java.util.List;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterApplyChatHistory implements AskMessage<Void>, RouterMessageOnlyActive {

    private Peer peer;
    private List<Message> messages;
    private Long maxReceiveDate;
    private Long maxReadDate;
    private boolean isEnded;

    public RouterApplyChatHistory(Peer peer, List<Message> messages, Long maxReceiveDate,
                                  Long maxReadDate, boolean isEnded) {
        this.peer = peer;
        this.messages = messages;
        this.maxReceiveDate = maxReceiveDate;
        this.maxReadDate = maxReadDate;
        this.isEnded = isEnded;
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

    public boolean isEnded() {
        return isEnded;
    }
}
