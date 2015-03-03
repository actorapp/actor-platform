package im.actor.model.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.storage.ListEngineItem;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class Message extends BserObject implements ListEngineItem {

    public static Message fromBytes(byte[] data) throws IOException {
        return Bser.parse(new Message(), data);
    }

    private long rid;
    private long sortDate;
    private long date;
    private int senderId;
    private MessageState messageState;
    private AbsContent content;

    public Message(long rid, long sortDate, long date, int senderId, MessageState messageState, AbsContent content) {
        this.rid = rid;
        this.sortDate = sortDate;
        this.date = date;
        this.senderId = senderId;
        this.messageState = messageState;
        this.content = content;
    }

    private Message() {

    }

    public long getRid() {
        return rid;
    }

    public long getSortDate() {
        return sortDate;
    }

    public long getDate() {
        return date;
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
        return new Message(rid, sortDate, date, senderId, messageState, content);
    }

    public Message changeDate(long date) {
        return new Message(rid, sortDate, date, senderId, messageState, content);
    }

    public Message changeContent(AbsContent content) {
        return new Message(rid, sortDate, date, senderId, messageState, content);
    }

    @Override
    public long getListId() {
        return rid;
    }

    @Override
    public long getListSortKey() {
        return sortDate;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        rid = values.getLong(1);
        sortDate = values.getLong(2);
        date = values.getLong(3);
        senderId = values.getInt(4);
        messageState = MessageState.fromValue(values.getInt(5));
        content = AbsContent.contentFromBytes(values.getBytes(6));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, rid);
        writer.writeLong(2, sortDate);
        writer.writeLong(3, date);
        writer.writeInt(4, senderId);
        writer.writeInt(5, messageState.getValue());
        writer.writeObject(6, content);
    }
}
