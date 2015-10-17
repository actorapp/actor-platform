/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto.bouncycastle;

import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;

import im.actor.runtime.CryptoRuntime;
public class BouncyCastleRuntime implements CryptoRuntime {

    @Override
    public byte[] MD5(byte[] data) {
        MD5Digest digest = new MD5Digest();
        digest.update(data, 0, data.length);
        byte[] res = new byte[16];
        digest.doFinal(res, 0);
        return res;
    }

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
}
