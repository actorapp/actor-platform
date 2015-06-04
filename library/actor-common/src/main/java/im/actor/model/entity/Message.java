/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserCreator;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.engine.ListEngineItem;
import im.actor.model.entity.content.AbsContent;

public class Message extends BserObject implements ListEngineItem {

    public static Message fromBytes(byte[] data) throws IOException {
        return Bser.parse(new Message(), data);
    }

    public static final BserCreator<Message> CREATOR = new BserCreator<Message>() {
        @Override
        public Message createInstance() {
            return new Message();
        }
    };

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

    public boolean isSent() {
        return messageState == MessageState.SENT || messageState == MessageState.SENT;
    }

    public boolean isReceivedOrSent() {
        return messageState == MessageState.SENT || messageState == MessageState.RECEIVED;
    }

    public boolean isPendingOrSent() {
        return messageState == MessageState.SENT || messageState == MessageState.PENDING;
    }

    public boolean isOnServer() {
        return messageState != MessageState.ERROR && messageState != MessageState.PENDING;
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

    public Message changeAllDate(long date) {
        return new Message(rid, date, date, senderId, messageState, content);
    }

    public Message changeContent(AbsContent content) {
        return new Message(rid, sortDate, date, senderId, messageState, content);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        rid = values.getLong(1);
        sortDate = values.getLong(2);
        date = values.getLong(3);
        senderId = values.getInt(4);
        messageState = MessageState.fromValue(values.getInt(5));
        content = AbsContent.parse(values.getBytes(6));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, rid);
        writer.writeLong(2, sortDate);
        writer.writeLong(3, date);
        writer.writeInt(4, senderId);
        writer.writeInt(5, messageState.getValue());
        writer.writeBytes(6, AbsContent.serialize(content));
    }

    @Override
    public long getEngineId() {
        return rid;
    }

    @Override
    public long getEngineSort() {
        return sortDate;
    }

    @Override
    public String getEngineSearch() {
        return null;
    }
}
