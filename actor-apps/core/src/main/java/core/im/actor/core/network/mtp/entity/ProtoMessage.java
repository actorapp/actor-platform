/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

import java.io.IOException;

public class ProtoMessage extends ProtoObject {

    private long messageId;

    private byte[] payload;

    public ProtoMessage(long messageId, byte[] payload) {
        this.messageId = messageId;
        this.payload = payload;
    }

    public long getMessageId() {
        return messageId;
    }

    public byte[] getPayload() {
        return payload;
    }

    public ProtoMessage(DataInput stream) throws IOException {
        super(stream);
    }

    @Override
    public void writeObject(DataOutput bs) throws IOException {
        bs.writeLong(messageId);
        bs.writeProtoBytes(payload, 0, payload.length);
    }

    @Override
    public ProtoObject readObject(DataInput bs) throws IOException {
        messageId = bs.readLong();
        payload = bs.readProtoBytes();
        return this;
    }

    @Override
    public String toString() {
        return "ProtoMessage [#" + messageId + "]";
    }
}
