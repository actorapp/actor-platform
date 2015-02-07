package im.actor.model.network.mtp.entity.rpc;


import im.actor.model.network.mtp.entity.ProtoStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;


/**
 * Created by ex3ndr on 03.09.14.
 */
public class RpcFloodWait extends ProtoStruct {
    public static final byte HEADER = (byte) 0x03;

    private int delay;

    public RpcFloodWait(InputStream stream) throws IOException {
        super(stream);
    }

    public RpcFloodWait(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public int getLength() {
        return 1 + 4;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(OutputStream bs) throws IOException {
        writeInt(delay, bs);
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        delay = readInt(bs);
    }
}
