package im.actor.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;

import static im.actor.crypto.Utils.substring;

/**
 * Created by ex3ndr on 19.10.14.
 */
public class RsaDecryptCipher {
    private PrivateKey privateKey;
    private Cipher rsaCipher;
    private Cipher aesCipher;

    public RsaDecryptCipher(PrivateKey privateKey) {
        this.privateKey = privateKey;
        this.rsaCipher = Crypto.createRSACipher();
        try {
            this.rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            throw new AssertionError(e);
        }
        this.aesCipher = Crypto.createAESCipher();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public byte[] decrypt(byte[] encryptedAesKey, byte[] encrypted) throws DecryptException {
        byte[] aesKey;
        try {
            aesKey = rsaCipher.doFinal(encryptedAesKey);
        } catch (BadPaddingException e) {
            throw new DecryptException(e);
        } catch (IllegalBlockSizeException e) {
            throw new DecryptException(e);
        }

        if (aesKey.length < 32 + 16) {
            throw new DecryptException("Too short aes key");
        }

        // Last bytes from decrypted aes key
        byte[] key = substring(aesKey, aesKey.length - 16 - 32, 32);
        byte[] iv = substring(aesKey, aesKey.length - 16, 16);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        byte[] res;
        try {
            aesCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            res = aesCipher.doFinal(encrypted);
        } catch (InvalidKeyException e) {
            throw new DecryptException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new DecryptException(e);
        } catch (BadPaddingException e) {
            throw new DecryptException(e);
        } catch (IllegalBlockSizeException e) {
            throw new DecryptException(e);
        }

        int len = Utils.readInt(res, 0);
        if (len <= 0) {
            throw new DecryptException("Incorrect package size");
        }
        if (len >= Config.MAX_PACKAGE_SIZE) {
            throw new DecryptException("Too big package size");
        }
        if (len > res.length - 4) {
            throw new DecryptException("Encrypted size are bigger than size");
        }
        res = substring(res, 4, len);

        return res;
    }
}
