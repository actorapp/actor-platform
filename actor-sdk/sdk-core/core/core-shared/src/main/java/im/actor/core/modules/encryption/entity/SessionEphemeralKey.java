package im.actor.core.modules.encryption.entity;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class SessionEphemeralKey extends BserObject {

    private byte[] publicKey;
    private byte[] privateKey;
    private long dateCreated;

    public SessionEphemeralKey(byte[] publicKey, byte[] privateKey, long dateCreated) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.dateCreated = dateCreated;
    }

    public SessionEphemeralKey(byte[] data) throws IOException {
        load(data);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        dateCreated = values.getLong(1);
        publicKey = values.getBytes(2);
        privateKey = values.optBytes(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, dateCreated);
        writer.writeBytes(2, publicKey);
        writer.writeBytes(3, privateKey);
    }
}
