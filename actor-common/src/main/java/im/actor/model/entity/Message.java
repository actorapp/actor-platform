package im.actor.model.entity;

import im.actor.model.entity.content.AbsContent;
import im.actor.model.mvvm.KeyValueItem;
import im.actor.model.mvvm.ListEngineItem;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class Message implements ListEngineItem {
    private final long rid;
    private final long sortKey;
    private final long time;
    private final int senderId;
    private final MessageState messageState;
    private final AbsContent content;

    public Message(long rid, long sortKey, long time, int senderId, MessageState messageState, AbsContent content) {
        this.rid = rid;
        this.sortKey = sortKey;
        this.time = time;
        this.senderId = senderId;
        this.messageState = messageState;
        this.content = content;
    }

    public long getRid() {
        return rid;
    }

    public long getSortKey() {
        return sortKey;
    }

    public long getTime() {
        return time;
    }

    public int getSenderId() {
        return senderId;
    }

    public MessageState getMessageState() {
        return messageState;
    }

    public AbsContent getContent() {
        return content;
    }

    public Message changeState(MessageState messageState) {
        return new Message(rid, sortKey, time, senderId, messageState, content);
    }

    public Message changeTime(long time) {
        return new Message(rid, sortKey, time, senderId, messageState, content);
    }

    @Override
    public long getListId() {
        return rid;
    }

    @Override
    public long getSortingKey() {
        return sortKey;
    }
}
