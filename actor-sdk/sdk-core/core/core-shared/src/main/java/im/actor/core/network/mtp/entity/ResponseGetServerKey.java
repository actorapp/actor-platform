package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class ResponseGetServerKey extends ProtoStruct {

    public static final int HEADER = 0xE3;

    private long keyId;
    private byte[] key;

    public ResponseGetServerKey(DataInput stream) throws IOException {
        super(stream);
    }

    public ResponseGetServerKey(long keyId, byte[] key) {
        this.keyId = keyId;
        this.key = key;
    }

    public long getKeyId() {
        return keyId;
    }

    public byte[] getKey() {
        return key;
    }

    @Override
    protected byte getHeader() {
        return (byte) HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(keyId);
        bs.writeProtoBytes(key, 0, key.length);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        keyId = bs.readLong();
        key = bs.readProtoBytes();
    }
}
