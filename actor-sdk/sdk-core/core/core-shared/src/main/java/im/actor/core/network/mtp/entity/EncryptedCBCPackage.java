package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class EncryptedCBCPackage extends ProtoObject {

    private byte[] iv;
    private byte[] encryptedContent;

    public EncryptedCBCPackage(byte[] iv, byte[] encryptedContent) {
        this.iv = iv;
        this.encryptedContent = encryptedContent;
    }

    public EncryptedCBCPackage(DataInput stream) throws IOException {
        super(stream);
    }

    public byte[] getIv() {
        return iv;
    }

    public byte[] getEncryptedContent() {
        return encryptedContent;
    }

    @Override
    public void writeObject(DataOutput bs) throws IOException {
        bs.writeProtoBytes(iv, 0, iv.length);
        bs.writeProtoBytes(encryptedContent, 0, encryptedContent.length);
    }

    @Override
    public ProtoObject readObject(DataInput bs) throws IOException {
        iv = bs.readProtoBytes();
        encryptedContent = bs.readProtoBytes();
        return this;
    }
}