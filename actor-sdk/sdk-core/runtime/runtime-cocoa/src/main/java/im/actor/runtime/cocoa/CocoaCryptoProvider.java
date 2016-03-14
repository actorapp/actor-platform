/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.cocoa;

import im.actor.runtime.cocoa.crypto.CocoaCryptoProxyProvider;
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
}
