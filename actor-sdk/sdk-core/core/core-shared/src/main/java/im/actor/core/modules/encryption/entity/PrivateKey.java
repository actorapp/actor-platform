package im.actor.core.modules.encryption.entity;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class PrivateKey extends BserObject {

    private long keyId;
    private String keyAlg;
    private byte[] key;
    private Boolean isUploaded;

    public PrivateKey(long keyId, String keyAlg, byte[] key, Boolean isUploaded) {
        this.keyId = keyId;
        this.keyAlg = keyAlg;
        this.key = key;
        this.isUploaded = isUploaded;
    }

    public PrivateKey(long keyId, String keyAlg, byte[] key) {
        this(keyId, keyAlg, key, null);
    }

    public PrivateKey(byte[] data) throws IOException {
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

    public boolean isUploaded() {
        if (isUploaded == null) {
            return false;
        }
        return isUploaded;
    }

    public PrivateKey markAsUploaded() {
        return new PrivateKey(getKeyId(), getKeyAlg(), getKey(), true);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        keyId = values.getLong(1);
        keyAlg = values.getString(2);
        key = values.getBytes(3);
        isUploaded = values.optBool(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, keyId);
        writer.writeString(2, keyAlg);
        writer.writeBytes(3, key);
        if (isUploaded != null) {
            writer.writeBool(4, isUploaded);
        }
    }
}
