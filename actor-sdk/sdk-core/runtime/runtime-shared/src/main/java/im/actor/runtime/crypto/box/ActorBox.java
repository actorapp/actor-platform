package im.actor.runtime.crypto.box;


import im.actor.runtime.Crypto;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.primitives.aes.AESFastEngine;
import im.actor.runtime.crypto.primitives.kuznechik.KuznechikFastEngine;
import im.actor.runtime.crypto.primitives.padding.PKCS7Padding;
import im.actor.runtime.crypto.primitives.streebog.Streebog256;
import im.actor.runtime.crypto.primitives.util.ByteStrings;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

/**
 * Encrypted Actor Box. Encrypted and HMACed with AES-128-CBC-HMAC-SHA256 and then again
 * with Kuznechik-128-CBC-HMAC-Streebog256. Cipher Text is padded with PKCS#7.
 *
 * @author Steve Kite (steve@actor.im)
 */
public class ActorBox {

    /**
     * Opening Encrypted box
     *
     * @param header     plain-text header of a box
     * @param cipherText encrypted content
     * @param key        Box key
     * @return plain-text content
     * @throws IntegrityException
     */
    public static byte[] openBox(byte[] header, byte[] cipherText, ActorBoxKey key) throws IntegrityException {
        CBCHmacBox aesCipher =
                new CBCHmacBox(Crypto.createAES128(key.getKeyAES()), Crypto.createSHA256(), key.getMacAES());
        CBCHmacBox kuzCipher =
                new CBCHmacBox(new KuznechikFastEngine(key.getKeyKuz()), new Streebog256(), key.getMacKuz());
        byte[] kuzPackage = aesCipher.decryptPackage(header,
                ByteStrings.substring(cipherText, 0, 16),
                ByteStrings.substring(cipherText, 16, cipherText.length - 16));
        byte[] plainText = kuzCipher.decryptPackage(header,
                ByteStrings.substring(kuzPackage, 0, 16),
                ByteStrings.substring(kuzPackage, 16, kuzPackage.length - 16));

        // Validating padding
        int paddingSize = plainText[plainText.length - 1] & 0xFF;
        if (paddingSize < 0 || paddingSize >= 16) {
            throw new IntegrityException("Incorrect padding!");
        }
        PKCS7Padding padding = new PKCS7Padding();
        if (!padding.validate(plainText, plainText.length - 1 - paddingSize, paddingSize)) {
            throw new IntegrityException("Padding does not match!");
        }

        return ByteStrings.substring(plainText, 0, plainText.length - 1 - paddingSize);
    }

    /**
     * Closing encrypted box
     *
     * @param header    plain-text header of a box
     * @param plainText plain-text content
     * @param random32  32 random bytes
     * @param key       Box key
     * @return encrypted context
     * @throws IntegrityException
     */
    public static byte[] closeBox(byte[] header, byte[] plainText, byte[] random32, ActorBoxKey key) throws IntegrityException {
        CBCHmacBox aesCipher = new CBCHmacBox(Crypto.createAES128(key.getKeyAES()), Crypto.createSHA256(), key.getMacAES());
        CBCHmacBox kuzCipher = new CBCHmacBox(new KuznechikFastEngine(key.getKeyKuz()), new Streebog256(), key.getMacKuz());

        // Calculating padding
        int paddingSize = (plainText.length + 1) % 16;
        byte[] paddedPlainText = new byte[plainText.length + 1 + paddingSize];
        ByteStrings.write(paddedPlainText, 0, plainText, 0, plainText.length);
        paddedPlainText[paddedPlainText.length - 1] = (byte) paddingSize;
        PKCS7Padding padding = new PKCS7Padding();
        padding.padding(paddedPlainText, plainText.length, paddingSize);

        byte[] kuzIv = ByteStrings.substring(random32, 0, 16);
        byte[] aesIv = ByteStrings.substring(random32, 16, 16);
        byte[] kuzPackage = ByteStrings.merge(kuzIv, kuzCipher.encryptPackage(header, kuzIv, paddedPlainText));
        return ByteStrings.merge(aesIv, aesCipher.encryptPackage(header, aesIv, kuzPackage));
    }
}
