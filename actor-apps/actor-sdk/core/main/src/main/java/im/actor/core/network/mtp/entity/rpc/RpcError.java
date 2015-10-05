/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity.rpc;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.core.network.mtp.entity.ProtoStruct;

public class RpcError extends ProtoStruct {

    public static final byte HEADER = (byte) 0x02;

    public int errorCode;
    public String errorTag;
    public String userMessage;
    public boolean canTryAgain;
    public byte[] relatedData;

    public RpcError(DataInput stream) throws IOException {
        super(stream);
    }

    public RpcError(int errorCode, String errorTag, String userMessage, boolean canTryAgain, byte[] relatedData) {
        this.errorCode = errorCode;
        this.errorTag = errorTag;
        this.userMessage = userMessage;
        this.canTryAgain = canTryAgain;
        this.relatedData = relatedData;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeInt(errorCode);
        bs.writeProtoString(errorTag);
        bs.writeProtoString(userMessage);
        bs.writeProtoBool(canTryAgain);
        bs.writeProtoBytes(relatedData, 0, relatedData.length);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        errorCode = bs.readInt();
        errorTag = bs.readProtoString();
        userMessage = bs.readProtoString();
        canTryAgain = bs.readProtoBool();
        relatedData = bs.readProtoBytes();
    }

    @Override
    public String toString() {
        return "RpcError [#" + errorCode + " " + errorTag + "]";
    }
}
