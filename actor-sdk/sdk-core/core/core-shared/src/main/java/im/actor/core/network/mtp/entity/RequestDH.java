package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class RequestDH extends ProtoStruct {

    public static final int HEADER = 0xE6;

    private long randomId;
    private long keyId;
    private byte[] clientNonce;
    private byte[] clientKey;

    public RequestDH(DataInput stream) throws IOException {
        super(stream);
    }

    public RequestDH(long randomId, long keyId, byte[] clientNonce, byte[] clientKey) {
        this.randomId = randomId;
        this.keyId = keyId;
        this.clientNonce = clientNonce;
        this.clientKey = clientKey;
    }

    @Override
    protected byte getHeader() {
        return (byte) HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(randomId);
        bs.writeLong(keyId);
        bs.writeProtoBytes(clientNonce, 0, clientNonce.length);
        bs.writeProtoBytes(clientKey, 0, clientKey.length);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        randomId = bs.readLong();
        keyId = bs.readLong();
        clientNonce = bs.readProtoBytes();
        clientKey = bs.readProtoBytes();
    }
}
