/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity.rpc;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.core.network.mtp.entity.ProtoStruct;

public class RpcOk extends ProtoStruct {

    public static final byte HEADER = (byte) 0x01;

    public int responseType;
    public byte[] payload;

    public RpcOk(DataInput stream) throws IOException {
        super(stream);
    }

    public RpcOk(int responseType, byte[] payload) {
        this.responseType = responseType;
        this.payload = payload;
    }

    public int getResponseType() {
        return responseType;
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
        bs.writeInt(responseType);
        bs.writeProtoBytes(payload, 0, payload.length);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        responseType = bs.readInt();
        payload = bs.readProtoBytes();
    }


    @Override
    public String toString() {
        return "RpcOk{" + responseType + "]";
    }
}
