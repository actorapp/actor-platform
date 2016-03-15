package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class RequestGetServerKey extends ProtoStruct {

    public static final int HEADER = 0xE2;

    private long keyId;

    public RequestGetServerKey(long keyId) {
        this.keyId = keyId;
    }

    public RequestGetServerKey(DataInput stream) throws IOException {
        super(stream);
    }

    public long getKeyId() {
        return keyId;
    }

    @Override
    protected byte getHeader() {
        return (byte) HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(keyId);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        keyId = bs.readLong();
    }
}
