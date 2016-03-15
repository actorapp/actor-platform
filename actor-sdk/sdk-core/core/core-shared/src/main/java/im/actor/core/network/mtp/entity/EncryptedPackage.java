package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class EncryptedPackage extends ProtoObject {

    private long seqNumber;
    private byte[] encryptedPackage;

    public EncryptedPackage(DataInput stream) throws IOException {
        super(stream);
    }


    public EncryptedPackage(long seqNumber, byte[] encryptedPackage) {
        this.seqNumber = seqNumber;
        this.encryptedPackage = encryptedPackage;
    }

    public long getSeqNumber() {
        return seqNumber;
    }

    public byte[] getEncryptedPackage() {
        return encryptedPackage;
    }

    @Override
    public void writeObject(DataOutput bs) throws IOException {
        bs.writeLong(seqNumber);
        bs.writeProtoBytes(encryptedPackage, 0, encryptedPackage.length);
    }

    @Override
    public ProtoObject readObject(DataInput bs) throws IOException {
        seqNumber = bs.readLong();
        encryptedPackage = bs.readProtoBytes();
        return this;
    }
}
