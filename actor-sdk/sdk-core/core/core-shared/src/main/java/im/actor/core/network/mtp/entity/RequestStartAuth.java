package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class RequestStartAuth extends ProtoStruct {

    public static final int HEADER = 0xE0;

    private long randomId;

    public RequestStartAuth(DataInput stream) throws IOException {
        super(stream);
    }

    public RequestStartAuth(long randomId) {
        this.randomId = randomId;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(randomId);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        randomId = bs.readLong();
    }

    @Override
    protected byte getHeader() {
        return (byte) HEADER;
    }

    @Override
    public String toString() {
        return "RequestStartAuth{" + randomId + "}";
    }
}
