/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.mtp.entity;

import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;

import java.io.IOException;

public class MTRpcRequest extends ProtoStruct {

    public static final byte HEADER = (byte) 0x03;

    public byte[] payload;

    public MTRpcRequest(DataInput stream) throws IOException {
        super(stream);
    }

    public MTRpcRequest(byte[] payload) {
        this.payload = payload;
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
        return "RequestBox";
    }
}
