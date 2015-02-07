package im.actor.model.network.mtp.entity.rpc;

import im.actor.model.network.mtp.entity.ProtoStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

/**
 * Created by ex3ndr on 03.09.14.
 */
public class RpcInternalError extends ProtoStruct {
    public static final byte HEADER = (byte) 0x04;

    private boolean canTryAgain;
    private int tryAgainDelay;

    public RpcInternalError(InputStream stream) throws IOException {
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
    public int getLength() {
        return 1 + 1 + 4;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(OutputStream bs) throws IOException {
        writeProtoBool(canTryAgain, bs);
        writeInt(tryAgainDelay, bs);
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        canTryAgain = readProtoBool(bs);
        tryAgainDelay = readInt(bs);
    }
}
