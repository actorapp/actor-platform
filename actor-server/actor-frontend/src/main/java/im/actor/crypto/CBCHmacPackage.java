package im.actor.crypto;

import im.actor.crypto.primitives.BlockCipher;
import im.actor.crypto.primitives.ByteStrings;
import im.actor.crypto.primitives.Hash;
import im.actor.crypto.primitives.Padding;
import im.actor.crypto.primitives.bc.hash.SHA256;
import im.actor.crypto.primitives.block.CBCCipher;
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

    private final CBCCipher cbcCipher;
    private final BlockCipher baseCipher;
    private final Hash baseHash;
    private final byte[] hmacKey;
    private final Padding padding;

    public CBCHmacPackage(BlockCipher baseCipher, Hash baseHash, byte[] hmacKey) {
        this.cbcCipher = new CBCCipher(baseCipher);
        this.baseCipher = baseCipher;
        this.baseHash = baseHash;
        this.hmacKey = hmacKey;
        this.padding = new TLSPadding();
    }

    public byte[] encryptPackage(byte[] iv, byte[] content) {
        int paddingLength = 0;
        int length =/*Hash size*/ 32 + /*Length prefix*/ 4 + content.length + /*padding length prefix*/1;
        if (length % 32 != 0) {
            paddingLength = 32 - length % 32;
            length += paddingLength;
        }

        byte[] res = new byte[length];
        ByteStrings.write(res, 0, ByteStrings.intToBytes(content.length), 0, 4);
        ByteStrings.write(res, 4, content, 0, content.length);

        HMAC.hmac(hmacKey, res, 0, content.length + 4, res, content.length + 4, new SHA256());
        padding.padding(res, res.length - paddingLength, paddingLength);

        return cbcCipher.encrypt(iv, res);
    }

    public byte[] decryptPackage(byte[] iv, byte[] encryptedContent) {
        byte[] content = cbcCipher.decrypt(iv, encryptedContent);

        byte[] hmac = new byte[32];
        int length = ByteStrings.bytesToInt(content);
        HMAC.hmac(hmacKey, content, 4, length, hmac, 0, new SHA256());
        for (int i = 0; i < 32; i++) {
            if (hmac[i] != content[length + 4 + i]) {
                throw new RuntimeException("Broken package!");
            }
        }

        // TODO: Validate
        int padding = content[content.length - 1] & 0xFF;
        for (int i = 0; i < padding; i++) {
            if ((content[content.length - 1 - i] & 0xFF) != padding) {
                throw new RuntimeException("Broken package!");
            }
        }

        return ByteStrings.substring(content, 4, length);
    }
}