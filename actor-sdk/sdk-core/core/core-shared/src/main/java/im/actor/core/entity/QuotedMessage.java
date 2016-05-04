/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;
import im.actor.core.api.ApiQuotedMessage;
import im.actor.core.entity.content.AbsContent;
import im.actor.runtime.bser.*;
import im.actor.runtime.storage.ListEngineItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuotedMessage extends BserObject {

    public static QuotedMessage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new QuotedMessage(), data);
    }

    @Property("readonly, nonatomic")
    private long messageId;
    @Property("readonly, nonatomic")
    private int publicGroupId;
    @Property("readonly, nonatomic")
    private int senderId;
    @Property("readonly, nonatomic")
    private long date;
    @Property("readonly, nonatomic")
    private AbsContent content;

    public QuotedMessage(long messageId, int publicGroupId, int senderId, long date, AbsContent content) {

        this.messageId = messageId;
        this.publicGroupId = publicGroupId;
        this.senderId = senderId;
        this.date = date;
        this.content = content;
    }

    protected QuotedMessage() {

    }

    public long getMessageId() {
        return messageId;
    }

    public int getPublicGroupId() {
        return publicGroupId;
    }

    public int getSenderId() {
        return senderId;
    }

    public long getDate() {
        return date;
    }

    public AbsContent getContent() {
        return content;
    }

    public QuotedMessage changeContent(AbsContent content) {
        return new QuotedMessage(messageId, publicGroupId, senderId, date, content);
    }


    @Override
    public void parse(BserValues values) throws IOException {
        messageId = values.getLong(1);
        publicGroupId = values.getInt(2);
        senderId = values.getInt(3);
        date = values.getLong(4);
        if (values.optBytes(5) != null)
            content = AbsContent.parse(values.optBytes(5));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, messageId);
        writer.writeInt(2, publicGroupId);
        writer.writeInt(3, senderId);
        writer.writeLong(4, date);
        if (content != null)
            writer.writeBytes(5, AbsContent.serialize(content));
    }

}
