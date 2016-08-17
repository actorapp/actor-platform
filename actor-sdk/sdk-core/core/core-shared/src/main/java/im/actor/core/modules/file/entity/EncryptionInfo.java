package im.actor.core.modules.file.entity;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class EncryptionInfo extends BserObject {

    public static EncryptionInfo fromBytes(byte[] data) throws IOException {
        return Bser.parse(new EncryptionInfo(), data);
    }

    private byte[] encryptionKey;
    private byte[] macKey;

    public EncryptionInfo(byte[] encryptionKey, byte[] macKey) {
        this.encryptionKey = encryptionKey;
        this.macKey = macKey;
    }

    private EncryptionInfo() {

    }

    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

    public byte[] getMacKey() {
        return macKey;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        encryptionKey = values.getBytes(1);
        macKey = values.getBytes(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeBytes(1, encryptionKey);
        writer.writeBytes(2, macKey);
    }
}
