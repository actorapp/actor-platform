package im.actor.core.modules.encryption.entity;

import java.io.IOException;

import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class OwnPrivateKeyUploadable extends OwnPrivateKey {
    private boolean isUploaded;

    public OwnPrivateKeyUploadable(long keyId, String keyAlg, byte[] key, boolean isUploaded) {
        super(keyId, keyAlg, key);
        this.isUploaded = isUploaded;
    }

    public OwnPrivateKeyUploadable(byte[] data) throws IOException {
        super(data);
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public OwnPrivateKeyUploadable markAsUploaded() {
        return new OwnPrivateKeyUploadable(getKeyId(), getKeyAlg(), getKey(), true);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        isUploaded = values.getBool(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeBool(4, isUploaded);
    }
}
