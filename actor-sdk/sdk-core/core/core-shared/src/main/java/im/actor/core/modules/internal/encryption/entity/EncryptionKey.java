package im.actor.core.modules.internal.encryption.entity;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.crypto.Curve25519KeyPair;

public class EncryptionKey extends BserObject {

    private long keyId;
    private String keyAlg;
    private byte[] publicKey;
    private byte[] privateKey;

    public EncryptionKey(long keyId, String keyAlg, byte[] publicKey, byte[] privateKey) {
        this.keyId = keyId;
        this.keyAlg = keyAlg;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public EncryptionKey(long keyId, Curve25519KeyPair keyPair) {
        this(keyId, "curve25519", keyPair.getPublicKey(), keyPair.getPrivateKey());
    }

    public EncryptionKey(byte[] data) throws IOException {
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

    public byte[] getPrivateKey() {
        return privateKey;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        keyId = values.getLong(1);
        keyAlg = values.getString(2);
        publicKey = values.getBytes(3);
        privateKey = values.optBytes(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, keyId);
        writer.writeString(2, keyAlg);
        writer.writeBytes(3, publicKey);
        if (privateKey != null) {
            writer.writeBytes(4, privateKey);
        }
    }
}
