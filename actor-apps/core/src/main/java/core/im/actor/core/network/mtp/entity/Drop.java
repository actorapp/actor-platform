/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public class Drop extends ProtoStruct {

    public static final byte HEADER = (byte) 0x0D;

    public long messageId;

    public String message;

    public Drop(DataInput stream) throws IOException {
        super(stream);
    }

    public Drop(long messageId, String message) {
        this.messageId = messageId;
        this.message = message;
    }

    public long getMessageId() {
        return messageId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(messageId);
        bs.writeProtoString(message);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        messageId = bs.readLong();
        message = bs.readProtoString();
    }

    @Override
    public String toString() {
        return "Drop[" + message + "]";
    }
}
