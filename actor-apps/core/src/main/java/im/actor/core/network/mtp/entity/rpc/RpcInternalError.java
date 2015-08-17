/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity.rpc;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.core.network.mtp.entity.ProtoStruct;

public class RpcInternalError extends ProtoStruct {
    public static final byte HEADER = (byte) 0x04;

    private boolean canTryAgain;
    private int tryAgainDelay;

    public RpcInternalError(DataInput stream) throws IOException {
        super(stream);
    }

    public RpcInternalError(boolean canTryAgain, int tryAgainDelay) {
        this.canTryAgain = canTryAgain;
        this.tryAgainDelay = tryAgainDelay;
    }

    public boolean isCanTryAgain() {
        return canTryAgain;
    }

    public int getTryAgainDelay() {
        return tryAgainDelay;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeProtoBool(canTryAgain);
        bs.writeInt(tryAgainDelay);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        canTryAgain = bs.readProtoBool();
        tryAgainDelay = bs.readInt();
    }
}
