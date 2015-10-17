/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public class MTRpcResponse extends ProtoStruct {

    public static final byte HEADER = (byte) 0x04;

    private long messageId;
    private byte[] payload;

    public MTRpcResponse(DataInput stream) throws IOException {
        super(stream);
    }

    public long getMessageId() {
        return messageId;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(messageId);
        bs.writeProtoBytes(payload, 0, payload.length);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        messageId = bs.readLong();
        payload = bs.readProtoBytes();
    }

    @Override
    public String toString() {
        return "ResponseBox [" + messageId + "]";
    }
}
