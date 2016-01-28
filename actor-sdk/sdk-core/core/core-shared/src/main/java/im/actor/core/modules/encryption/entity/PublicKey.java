package im.actor.core.modules.encryption.entity;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.crypto.Curve25519KeyPair;

public class PublicKey extends BserObject {

    private long keyId;
    private String keyAlg;
    private byte[] publicKey;

    public PublicKey(long keyId, String keyAlg, byte[] publicKey) {
        this.keyId = keyId;
        this.keyAlg = keyAlg;
        this.publicKey = publicKey;
    }

    public PublicKey(byte[] data) throws IOException {
        load(data);
    }

    public long getKeyId() {
        return keyId;
    }

    public String getKeyAlg() {
        return keyAlg;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        keyId = values.getLong(1);
        keyAlg = values.getString(2);
        publicKey = values.getBytes(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, keyId);
        writer.writeString(2, keyAlg);
        writer.writeBytes(3, publicKey);
    }
}
