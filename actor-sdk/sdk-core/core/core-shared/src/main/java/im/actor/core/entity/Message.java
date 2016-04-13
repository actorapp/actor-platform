/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.core.entity.content.AbsContent;

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

    public static final String ENTITY_NAME = "Message";

    @Property("readonly, nonatomic")
    private long rid;
    @Property("readonly, nonatomic")
    private long sortDate;
    @Property("readonly, nonatomic")
    private long date;
    @Property("readonly, nonatomic")
    private int senderId;
    @Property("readonly, nonatomic")
    private MessageState messageState;
    @Property("readonly, nonatomic")
    private AbsContent content;
    @Property("readonly, nonatomic")
    private List<Reaction> reactions;
    @Property("readonly, nonatomic")
    private int contentIndex;

    public Message(long rid, long sortDate, long date, int senderId, MessageState messageState, AbsContent content) {
        this(rid, sortDate, date, senderId, messageState, content, new ArrayList<Reaction>(), 0);
    }

    public Message(long rid, long sortDate, long date, int senderId, MessageState messageState, AbsContent content,
                   List<Reaction> reactions, int contentIndex) {
        this.rid = rid;
        this.sortDate = sortDate;
        this.date = date;
        this.senderId = senderId;
        this.messageState = messageState;
        this.content = content;
        this.reactions = reactions;
        this.contentIndex = contentIndex;
    }

    protected Message() {

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

    public List<Reaction> getReactions() {
        return reactions;
    }

    public int getContentIndex() {
        return contentIndex;
    }

    public boolean isSent() {
        return messageState == MessageState.SENT;
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
        return new Message(rid, sortDate, date, senderId, messageState, content, reactions, contentIndex);
    }

    public Message changeDate(long date) {
        return new Message(rid, sortDate, date, senderId, messageState, content, reactions, contentIndex);
    }

    public Message changeAllDate(long date) {
        return new Message(rid, date, date, senderId, messageState, content, reactions, contentIndex);
    }

    public Message changeContent(AbsContent content) {
        return new Message(rid, sortDate, date, senderId, messageState, content, reactions, contentIndex + 1);
    }

    public Message changeReactions(List<Reaction> reactions) {
        return new Message(rid, sortDate, date, senderId, messageState, content, reactions, contentIndex);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        rid = values.getLong(1);
        sortDate = values.getLong(2);
        date = values.getLong(3);
        senderId = values.getInt(4);
        messageState = MessageState.fromValue(values.getInt(5));
        content = AbsContent.parse(values.getBytes(6));
        reactions = new ArrayList<>();
        for (byte[] react : values.getRepeatedBytes(7)) {
            reactions.add(Reaction.fromBytes(react));
        }
        contentIndex = values.getInt(8, 0);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, rid);
        writer.writeLong(2, sortDate);
        writer.writeLong(3, date);
        writer.writeInt(4, senderId);
        writer.writeInt(5, messageState.getValue());
        writer.writeBytes(6, AbsContent.serialize(content));
        writer.writeRepeatedObj(7, reactions);
        writer.writeInt(8, contentIndex);
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
