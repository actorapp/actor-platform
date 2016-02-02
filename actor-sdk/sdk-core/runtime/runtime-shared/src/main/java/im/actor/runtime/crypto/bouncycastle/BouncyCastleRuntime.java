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
    public byte[] fromHex(String hex) {
        byte[] res = new byte[hex.length() / 2];
        for (int j = 0; j < hex.length() / 2; j++) {
            String dg = hex.charAt(j * 2) + "" + hex.charAt(j * 2 + 1);
            res[j] = (byte) Integer.parseInt(dg, 16);
        }
        return res;
    }

    @Override
    public byte[] fromHexReverse(String hex) {
        byte[] res = new byte[hex.length() / 2];
        for (int j = 0; j < hex.length() / 2; j++) {
            String dg = hex.charAt(j * 2) + "" + hex.charAt(j * 2 + 1);
            res[res.length - j - 1] = (byte) Integer.parseInt(dg, 16);
        }
        return res;
    }

    private static final String HEXES = "0123456789ABCDEF";

    @Override
    public String toHex(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
}
