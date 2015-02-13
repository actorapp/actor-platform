package im.actor.messenger.core.actors.send;

import com.droidkit.bser.Bser;

import im.actor.api.scheme.encrypted.AudioExtension;
import im.actor.api.scheme.encrypted.EncryptionType;
import im.actor.api.scheme.encrypted.FileMessage;
import im.actor.api.scheme.encrypted.PhotoExtension;
import im.actor.api.scheme.encrypted.PlainFileLocation;
import im.actor.api.scheme.encrypted.PlainMessage;
import im.actor.api.scheme.encrypted.PlainPackage;
import im.actor.api.scheme.encrypted.TextMessage;
import im.actor.api.scheme.encrypted.VideoExtension;
import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.messages.FastThumb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

import static im.actor.messenger.util.io.StreamingUtils.writeInt;
import static im.actor.messenger.util.io.StreamingUtils.writeProtoBytes;

/**
 * Created by ex3ndr on 04.10.14.
 */
public class EncryptedMessages {

    private static final int TYPE_PLAIN_MESSAGE = 1;

    private static final int TYPE_TEXT = 1;
    private static final int TYPE_FILE = 2;

    public static byte[] createTextMessage(long randomId, String text) {
        return createMessage(randomId, TYPE_TEXT, new TextMessage(text, 0, null).toByteArray());
    }

    private static PlainFileLocation createFileLocation(FileLocation fileLocation) {
        EncryptionType encryptionType;
        switch (fileLocation.getEncryption()) {
            case AES:
                encryptionType = EncryptionType.AES;
                break;
            case AES_THEN_MAC:
                encryptionType = EncryptionType.AES_THEN_MAC;
                break;
            default:
            case NONE:
                encryptionType = EncryptionType.NONE;
                break;
        }

        return new PlainFileLocation(fileLocation.getFileId(), fileLocation.getAccessHash(),
                fileLocation.getFileSize(), encryptionType, fileLocation.getEncryptedFileSize(),
                fileLocation.getEncryptionKey());
    }

    public static byte[] createFileMessage(long randomId, FileLocation fileLocation, String name,
                                           int extensionType, byte[] extension, byte[] thumb) {
        try {
            im.actor.api.scheme.encrypted.FastThumb fastThumb2 = null;
            if (thumb != null) {
                FastThumb fastThumb = Bser.parse(FastThumb.class, thumb);
                fastThumb2 = new im.actor.api.scheme.encrypted.FastThumb(
                        fastThumb.getW(), fastThumb.getH(), fastThumb.getImage());
            }

            String mimeType;

            if (extensionType == 0x01) {
                mimeType = "image/jpeg";
            } else if (extensionType == 0x02) {
                mimeType = "video/mp4";
            } else if (extensionType == 0x03) {
                mimeType = "audio/ogg";
            } else {
                mimeType = "application/octet-stream";
            }

            return createMessage(randomId, TYPE_FILE, new FileMessage(name, mimeType,
                    createFileLocation(fileLocation), fastThumb2, extensionType, extension).toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] photoMetadata(int w, int h) {
        return new PhotoExtension(w, h).toByteArray();
    }

    public static byte[] videoMetadata(int duration, int w, int h) {
        return new VideoExtension(w, h, duration).toByteArray();
    }

    public static byte[] opusMetadata(int duration) {
        return new AudioExtension(duration).toByteArray();
    }

    public static byte[] createMessage(long randomId, int type, byte[] message) {
        // Decrypted Message
        byte[] decryptedMessage = new PlainMessage(randomId, type, message).toByteArray();

        // Decrypted Data
        long crc32Val = 0;
        try {
            CRC32 crc32 = new CRC32();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            writeInt(TYPE_PLAIN_MESSAGE, stream);
            writeProtoBytes(message, stream);
            crc32.update(stream.toByteArray());
            crc32Val = crc32.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new PlainPackage(TYPE_PLAIN_MESSAGE, decryptedMessage, crc32Val).toByteArray();
    }
}
