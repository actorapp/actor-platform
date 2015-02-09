package im.actor.model.network.mtp.entity.rpc;

import im.actor.model.network.mtp.entity.ProtoStruct;
import im.actor.model.util.DataInput;
import im.actor.model.util.DataOutput;

import java.io.IOException;

/**
 * Created by ex3ndr on 03.09.14.
 */
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
