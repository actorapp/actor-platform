/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public class UnsentMessage extends ProtoStruct {

    public static final byte HEADER = (byte) 0x07;

    private long messageId;
    private int len;

    public UnsentMessage(long messageId, int len) {
        this.messageId = messageId;
        this.len = len;
    }

    public UnsentMessage(DataInput stream) throws IOException {
        super(stream);
    }

    public long getMessageId() {
        return messageId;
    }

    public int getLen() {
        return len;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(messageId);
        bs.writeInt(len);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        messageId = bs.readLong();
        len = bs.readInt();
    }

    @Override
    public String toString() {
        return "UnsentMessage[" + messageId + "]";
    }
}
