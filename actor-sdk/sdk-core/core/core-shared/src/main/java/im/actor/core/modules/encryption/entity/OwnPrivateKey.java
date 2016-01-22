package im.actor.core.modules.encryption.entity;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class OwnPrivateKey extends BserObject {

    private long keyId;
    private String keyAlg;
    private byte[] key;

    public OwnPrivateKey(long keyId, String keyAlg, byte[] key) {
        this.keyId = keyId;
        this.keyAlg = keyAlg;
        this.key = key;
    }

    public OwnPrivateKey(byte[] data) throws IOException {
        load(data);
    }

    public long getKeyId() {
        return keyId;
    }

    public String getKeyAlg() {
        return keyAlg;
    }

    public byte[] getKey() {
        return key;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        keyId = values.getLong(1);
        keyAlg = values.getString(2);
        key = values.getBytes(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, keyId);
        writer.writeString(2, keyAlg);
        writer.writeBytes(3, key);
    }
}
