package im.actor.model.crypto;

import im.actor.model.CryptoProvider;
import im.actor.model.crypto.bouncycastle.crypto.digest.SHA256Digest;
import im.actor.model.crypto.bouncycastle.crypto.digest.SHA512Digest;

/**
 * Created by ex3ndr on 27.02.15.
 */
public class BouncyCastleProvider implements CryptoProvider {
    @Override
    public byte[] SHA256(byte[] data) {
        SHA256Digest digest = new SHA256Digest();
        digest.update(data, 0, data.length);
        byte[] res = new byte[32];
        digest.doFinal(res, 0);
        return res;
    }

    @Override
    public byte[] SHA512(byte[] data) {
        SHA512Digest digest = new SHA512Digest();
        digest.update(data, 0, data.length);
        byte[] res = new byte[64];
        digest.doFinal(res, 0);
        return res;
    }

    // TODO: Implement
    @Override
    public byte[] randomBytes(int length) {
        return new byte[0];
    }

    @Override
    public int randomInt(int maxValue) {
        return 0;
    }
}
