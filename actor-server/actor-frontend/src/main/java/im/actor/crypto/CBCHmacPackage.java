package im.actor.crypto;

import im.actor.crypto.primitives.BlockCipher;
import im.actor.crypto.primitives.util.ByteStrings;
import im.actor.crypto.primitives.Digest;
import im.actor.crypto.primitives.Padding;
import im.actor.crypto.primitives.digest.SHA256;
import im.actor.crypto.primitives.modes.CBCBlockCipher;
import im.actor.crypto.primitives.hmac.HMAC;
import im.actor.crypto.primitives.padding.TLSPadding;

/**
 * CBC-encrypted package with HMAC (MAC-THEN-ENCRYPT).
 * <p/>
 * Package format:
 * 1) content.length[4 bytes]
 * 2) content[content.length]
 * 4) HMAC[HMAC.lenght]
 * 5) TLS-like padding
 * Then this package is encrypted with baseCipher in CBC mode
 */
public class CBCHmacPackage {

    private final CBCBlockCipher cbcBlockCipher;
    private final BlockCipher baseCipher;
    private final Digest baseDigest;
    private final HMAC hmac;
    private final byte[] hmacKey;
    private final Padding padding;

    public CBCHmacPackage(BlockCipher baseCipher, Digest baseDigest, byte[] hmacKey) {
        this.cbcBlockCipher = new CBCBlockCipher(baseCipher);
        this.baseCipher = baseCipher;
        this.baseDigest = baseDigest;
        this.hmacKey = hmacKey;
        this.padding = new TLSPadding();
        this.hmac = new HMAC(baseDigest);
    }

    public byte[] encryptPackage(byte[] iv, byte[] content) {
        int paddingLength = 0;
        int length =/*Digest size*/ 32 + /*Length prefix*/ 4 + content.length + /*padding length prefix*/1;
        if (length % 32 != 0) {
            paddingLength = 32 - length % 32;
            length += paddingLength;
        }

        byte[] res = new byte[length];
        ByteStrings.write(res, 0, ByteStrings.intToBytes(content.length), 0, 4);
        ByteStrings.write(res, 4, content, 0, content.length);

        hmac.calculate(hmacKey, res, 0, content.length + 4, res, content.length + 4);
        padding.padding(res, res.length - paddingLength - 1, paddingLength + 1);

        return cbcBlockCipher.encrypt(iv, res);
    }

    public byte[] decryptPackage(byte[] iv, byte[] encryptedContent) {
        byte[] content = cbcBlockCipher.decrypt(iv, encryptedContent);

        byte[] hmacValue = new byte[32];
        int length = ByteStrings.bytesToInt(content);
        hmac.calculate(hmacKey, content, 0, length + 4, hmacValue, 0);
        for (int i = 0; i < 32; i++) {
            if (hmacValue[i] != content[length + 4 + i]) {
                throw new RuntimeException("Broken package!");
            }
        }

        int paddingSize = content[content.length - 1] & 0xFF;
        if (!padding.validate(content, content.length - paddingSize, paddingSize)) {
            throw new RuntimeException("Broken package!");
        }

        return ByteStrings.substring(content, 4, length);
    }
}