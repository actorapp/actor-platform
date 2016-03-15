package im.actor.runtime.crypto.box;

import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.BlockCipher;
import im.actor.runtime.crypto.Digest;
import im.actor.runtime.crypto.primitives.Padding;
import im.actor.runtime.crypto.primitives.hmac.HMAC;
import im.actor.runtime.crypto.primitives.modes.CBCBlockCipher;
import im.actor.runtime.crypto.primitives.padding.PKCS7Padding;
import im.actor.runtime.crypto.primitives.util.ByteStrings;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

/**
 * CBC-encrypted package with HMAC (MAC-THEN-ENCRYPT).
 * <p/>
 * Package format:
 * 1) content.length[4 bytes]
 * 2) content[content.length]
 * 4) HMAC[HMAC.lenght]
 * 5) PCKS#7 padding
 * Then this package is encrypted with baseCipher in CBC mode
 *
 * @author Steve Kite (steve@actor.im)
 */
public class CBCHmacBox {

    private final CBCBlockCipher cbcBlockCipher;
    private final HMAC hmac;
    private final Padding padding;

    public CBCHmacBox(BlockCipher baseCipher, Digest baseDigest, byte[] hmacKey) {
        this.cbcBlockCipher = new CBCBlockCipher(baseCipher);
        this.padding = new PKCS7Padding();
        this.hmac = new HMAC(hmacKey, baseDigest);
    }

    public byte[] encryptPackage(byte[] header, byte[] iv, byte[] content) throws IntegrityException {
        if (iv.length != 16) {
            throw new IntegrityException("IV MUST be 16 bytes long!");
        }
        int paddingLength = 0;
        int length =/*Digest size*/ 32 + /*Length prefix*/ 4 + content.length + /*padding length prefix*/1;
        if (length % 32 != 0) {
            paddingLength = 32 - length % 32;
            length += paddingLength;
        }

        byte[] res = new byte[length];
        ByteStrings.write(res, 0, ByteStrings.intToBytes(content.length), 0, 4);
        ByteStrings.write(res, 4, content, 0, content.length);

        hmac.reset();
        hmac.update(header, 0, header.length);
        hmac.update(iv, 0, 16);
        hmac.update(res, 0, content.length + 4);
        hmac.doFinal(res, content.length + 4);
        padding.padding(res, res.length - paddingLength - 1, paddingLength);
        res[res.length - 1] = (byte) paddingLength;

        return cbcBlockCipher.encrypt(iv, res);
    }

    public byte[] decryptPackage(byte[] header, byte[] iv, byte[] encryptedContent) throws IntegrityException {
        if (iv.length != 16) {
            throw new IntegrityException("IV MUST be 16 bytes long!");
        }

        byte[] content = cbcBlockCipher.decrypt(iv, encryptedContent);

        byte[] hmacValue = new byte[32];
        int length = ByteStrings.bytesToInt(content);
        hmac.reset();
        hmac.update(header, 0, header.length);
        hmac.update(iv, 0, 16);
        hmac.update(content, 0, length + 4);
        hmac.doFinal(hmacValue, 0);
        for (int i = 0; i < 32; i++) {
            if (hmacValue[i] != content[length + 4 + i]) {
                throw new IntegrityException("Broken package!");
            }
        }

        int paddingSize = content[content.length - 1] & 0xFF;
        if (!padding.validate(content, content.length - paddingSize, paddingSize)) {
            throw new IntegrityException("Broken package!");
        }

        return ByteStrings.substring(content, 4, length);
    }


}