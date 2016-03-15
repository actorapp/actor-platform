package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class ResponseDoDH extends ProtoStruct {

    public static final int HEADER = 0xE7;

    private long randomId;
    private byte[] verify;
    private byte[] verifySign;

    public ResponseDoDH(DataInput stream) throws IOException {
        super(stream);
    }

    public ResponseDoDH(long randomId, byte[] verify, byte[] verifySign) {
        this.randomId = randomId;
        this.verify = verify;
        this.verifySign = verifySign;
    }

    public long getRandomId() {
        return randomId;
    }

    public byte[] getVerify() {
        return verify;
    }

    public byte[] getVerifySign() {
        return verifySign;
    }

    @Override
    protected byte getHeader() {
        return (byte) HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(randomId);
        bs.writeProtoBytes(verify, 0, verify.length);
        bs.writeProtoBytes(verifySign, 0, verifySign.length);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        randomId = bs.readLong();
        verify = bs.readProtoBytes();
        verifySign = bs.readProtoBytes();
    }
}
