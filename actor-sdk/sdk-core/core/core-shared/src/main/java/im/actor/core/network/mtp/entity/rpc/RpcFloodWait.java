/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity.rpc;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.core.network.mtp.entity.ProtoStruct;

public class RpcFloodWait extends ProtoStruct {
    public static final byte HEADER = (byte) 0x03;

    private int delay;

    public RpcFloodWait(DataInput stream) throws IOException {
        super(stream);
    }

    public RpcFloodWait(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeInt(delay);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        delay = bs.readInt();
    }
}
