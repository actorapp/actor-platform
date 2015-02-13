package im.actor.messenger.core.encryption;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

/**
 * Created by ex3ndr on 27.08.14.
 */
public class RsaEncryptionUtils {

    public static Cipher createRSACipher() {
        try {
            return Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        } catch (Exception e) {
            // We can't work without this
            throw new RuntimeException(e);
        }
    }

    public static byte[] encryptMessage(PublicKey publicKey, byte[] message) {
        try {
            Cipher cipher = createRSACipher();
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(message);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decryptMessage(PrivateKey privateKey, byte[] encryptedMessage) {
        try {
            Cipher cipher = createRSACipher();
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
