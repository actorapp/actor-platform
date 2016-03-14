package im.actor.runtime.cocoa.crypto;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.crypto.Digest;

public interface CocoaCryptoProxyProvider {

    @ObjectiveCName("createSHA256")
    Digest createSHA256();
}
