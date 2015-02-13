package im.actor.messenger.core.actors.encryption;

import com.droidkit.actors.concurrency.Future;

/**
 * Created by ex3ndr on 13.10.14.
 */
public interface FileEncryptionInt {
    public Future<byte[]> encryptFile(String sourceFile, String destFile);

    public Future<Boolean> decryptFile(String sourceFile, byte[] key, String destFile);

    public Future<Boolean> decryptFileAes(String sourceFile, byte[] key, String destFile);
}
