package im.actor.messenger.storage.scheme;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class FileLocation extends BserObject {

    public enum Encryption {
        NONE, AES, AES_THEN_MAC
    }

    private long fileId;
    private long accessHash;
    private int fileSize;

    private Encryption encryption;
    private int encryptedFileSize;
    private byte[] encryptionKey;

    public FileLocation(long fileId, long accessHash, int fileSize, Encryption encryption, int encryptedFileSize, byte[] encryptionKey) {
        this.fileId = fileId;
        this.accessHash = accessHash;
        this.fileSize = fileSize;
        this.encryption = encryption;
        this.encryptedFileSize = encryptedFileSize;
        this.encryptionKey = encryptionKey;
    }

    public FileLocation(long fileId, long accessHash, int fileSize) {
        this.fileId = fileId;
        this.accessHash = accessHash;
        this.fileSize = fileSize;
        this.encryption = Encryption.NONE;
        this.encryptedFileSize = fileSize;
        this.encryptionKey = new byte[0];
    }

    public FileLocation() {

    }

    public long getFileId() {
        return fileId;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public int getFileSize() {
        return fileSize;
    }

    public Encryption getEncryption() {
        return encryption;
    }

    public int getEncryptedFileSize() {
        return encryptedFileSize;
    }

    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        fileId = values.getLong(1);
        accessHash = values.getLong(2);
        fileSize = values.getInt(3);

        int rawType = values.getInt(4);
        switch (rawType) {
            default:
            case 1:
                encryption = Encryption.NONE;
                break;
            case 2:
                encryption = Encryption.AES;
                break;
            case 4:
                encryption = Encryption.AES_THEN_MAC;
                break;
        }

        encryptedFileSize = values.optInt(5);
        encryptionKey = values.optBytes(6);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, fileId);
        writer.writeLong(2, accessHash);
        writer.writeInt(3, fileSize);
        switch (encryption) {
            default:
            case NONE:
                writer.writeInt(4, 1);
                break;
            case AES:
                writer.writeInt(4, 2);
                break;
            case AES_THEN_MAC:
                writer.writeInt(4, 4);
                break;
        }
        writer.writeInt(5, encryptedFileSize);
        writer.writeBytes(6, encryptionKey);
    }
}
