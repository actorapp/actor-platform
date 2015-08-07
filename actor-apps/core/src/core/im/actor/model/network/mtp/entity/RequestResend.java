/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.mtp.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;

public class RequestResend extends ProtoStruct {

    public static final byte HEADER = (byte) 0x09;

    private long messageId;

    public RequestResend(long messageId) {
        this.messageId = messageId;
    }

    public RequestResend(DataInput stream) throws IOException {
        super(stream);
    }

    public long getMessageId() {
        return messageId;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(messageId);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        messageId = bs.readLong();
    }
}
