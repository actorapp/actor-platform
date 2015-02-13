package im.actor.messenger.core.actors.encryption;

import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.typed.TypedActor;
import im.actor.crypto.FileCipher;
import im.actor.crypto.ObsoleteFileCipher;
import im.actor.messenger.core.actors.base.TypedActorHolder;

import java.io.IOException;

/**
 * Created by ex3ndr on 13.10.14.
 */
public class FileEncryption extends TypedActor<FileEncryptionInt> implements FileEncryptionInt {

    private static final TypedActorHolder<FileEncryptionInt> HOLDER = new TypedActorHolder<FileEncryptionInt>(
            FileEncryptionInt.class, FileEncryption.class, "file_encryption", "file_encryption"
    );

    public static FileEncryptionInt fileEncryption() {
        return HOLDER.get();
    }

    public FileEncryption() {
        super(FileEncryptionInt.class);
    }

    @Override
    public Future<byte[]> encryptFile(String sourceFile, String destFile) {
        FileCipher cipher = new FileCipher();
        try {
            cipher.encrypt(sourceFile, destFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result(cipher.getKey());
    }

    @Override
    public Future<Boolean> decryptFile(String sourceFile, byte[] key, String destFile) {
        FileCipher cipher = new FileCipher(key);
        try {
            cipher.decrypt(sourceFile, destFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result(true);
    }

    @Override
    public Future<Boolean> decryptFileAes(String sourceFile, byte[] key, String destFile) {
        ObsoleteFileCipher cipher = new ObsoleteFileCipher(key);
        try {
            cipher.decrypt(sourceFile, destFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result(true);
    }
}
