package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class ResponseStartAuth extends ProtoStruct {

    public static final int HEADER = 0xE1;

    private long randomId;
    private long[] availableKeys;
    private byte[] serverNonce;

    public ResponseStartAuth(DataInput stream) throws IOException {
        super(stream);
    }

    public ResponseStartAuth(long randomId, long[] availableKeys, byte[] serverNonce) {
        this.randomId = randomId;
        this.availableKeys = availableKeys;
        this.serverNonce = serverNonce;
    }

    public long getRandomId() {
        return randomId;
    }

    public long[] getAvailableKeys() {
        return availableKeys;
    }

    public byte[] getServerNonce() {
        return serverNonce;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(randomId);
        bs.writeProtoLongs(availableKeys);
        bs.writeBytes(serverNonce);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        randomId = bs.readLong();
        availableKeys = bs.readProtoLongs();
        serverNonce = bs.readProtoBytes();
    }

    @Override
    protected byte getHeader() {
        return (byte) HEADER;
    }
}
