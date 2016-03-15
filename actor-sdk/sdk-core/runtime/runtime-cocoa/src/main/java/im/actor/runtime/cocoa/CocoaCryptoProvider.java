/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.cocoa;

import im.actor.runtime.cocoa.crypto.CocoaCryptoProxyProvider;
import im.actor.runtime.crypto.BlockCipher;
import im.actor.runtime.crypto.Digest;
import im.actor.runtime.generic.GenericCryptoProvider;

public class CocoaCryptoProvider extends GenericCryptoProvider {

    private static CocoaCryptoProxyProvider proxyProvider;

    public static CocoaCryptoProxyProvider getProxyProvider() {
        return proxyProvider;
    }

    public static void setProxyProvider(CocoaCryptoProxyProvider proxyProvider) {
        CocoaCryptoProvider.proxyProvider = proxyProvider;
    }

    @Override
    public Digest SHA256() {
        if (proxyProvider != null) {
            return proxyProvider.createSHA256();
        }
        return super.SHA256();
    }

    @Override
    public BlockCipher AES128(byte[] key) {
        if (proxyProvider != null) {
            return proxyProvider.createAES128(key);
        }
        return super.AES128(key);
    }
}
