/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity.rpc;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.core.network.mtp.entity.ProtoStruct;

public class RpcRequest extends ProtoStruct {

    public static final byte HEADER = (byte) 0x01;

    public int requestType;

    public byte[] payload;

    public RpcRequest(DataInput stream) throws IOException {
        super(stream);
    }

    public RpcRequest(int requestType, byte[] payload) {
        this.requestType = requestType;
        this.payload = payload;
    }

    public int getRequestType() {
        return requestType;
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
        bs.writeInt(requestType);
        bs.writeProtoBytes(payload, 0, payload.length);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        requestType = bs.readInt();
        payload = bs.readProtoBytes();
    }

    @Override
    public String toString() {
        return "RpcRequest[" + requestType + "]";
    }
}
