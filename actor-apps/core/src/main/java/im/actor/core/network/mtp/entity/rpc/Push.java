/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity.rpc;


import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.core.network.mtp.entity.ProtoStruct;

public class Push extends ProtoStruct {
    public int updateType;
    public byte[] body;

    public Push(DataInput stream) throws IOException {
        super(stream);
    }

    public Push(int updateType, byte[] body) {
        this.updateType = updateType;
        this.body = body;
    }

    @Override
    public byte getHeader() {
        return 0;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeInt(updateType);
        bs.writeProtoBytes(body, 0, body.length);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        updateType = bs.readInt();
        body = bs.readProtoBytes();
    }

    @Override
    public String toString() {
        return "Update[" + updateType + "]";
    }
}
