/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

import java.io.IOException;

public class MTPush extends ProtoStruct {

    public static final byte HEADER = (byte) 0x05;

    private byte[] payload;

    public MTPush(DataInput stream) throws IOException {
        super(stream);
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
        bs.writeProtoBytes(payload, 0, payload.length);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        payload = bs.readProtoBytes();
    }

    @Override
    public String toString() {
        return "UpdateBox";
    }
}
