package im.actor.runtime.cocoa.crypto;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.crypto.Digest;
import im.actor.runtime.crypto.BlockCipher;

public interface CocoaCryptoProxyProvider {

    @ObjectiveCName("createSHA256")
    Digest createSHA256();

    @ObjectiveCName("createAES128WithKey:")
    BlockCipher createAES128(byte[] key);
}
