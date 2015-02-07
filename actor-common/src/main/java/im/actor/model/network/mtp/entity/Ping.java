package im.actor.model.network.mtp.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

public class Ping extends ProtoStruct {

    public static final byte HEADER = (byte) 0x01;

    private long randomId;

    public Ping(InputStream stream) throws IOException {
        super(stream);
    }

    public Ping(long randomId) {
        this.randomId = randomId;
    }

    public long getRandomId() {
        return randomId;
    }

    @Override
    public int getLength() {
        return 1 + 8;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(OutputStream bs) throws IOException {
        writeLong(randomId, bs);
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        randomId = readLong(bs);
    }

    @Override
    public String toString() {
        return "Ping{" + randomId + "}";
    }
}
