package im.actor.runtime.crypto.ratchet;

import im.actor.runtime.Crypto;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.primitives.hmac.HMAC;
import im.actor.runtime.crypto.primitives.kdf.HKDF;
import im.actor.runtime.crypto.primitives.util.ByteStrings;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class RatchetMessageKey {
    public static ActorBoxKey buildKey(byte[] rootChainKey, int index) {
        HMAC hmac = new HMAC(rootChainKey, Crypto.createSHA256());
        byte[] indx = ByteStrings.intToBytes(index);
        hmac.update(indx, 0, indx.length);
        byte[] messageKey = new byte[32];
        hmac.doFinal(messageKey, 0);
        byte[] messageKeyExt = new HKDF(Crypto.createSHA256()).deriveSecrets(messageKey, 128);
        byte[] aesCipherKey = ByteStrings.substring(messageKeyExt, 0, 32);
        byte[] aesMacKey = ByteStrings.substring(messageKeyExt, 32, 32);
        byte[] kuzCipherKey = ByteStrings.substring(messageKeyExt, 64, 32);
        byte[] kuzMacKey = ByteStrings.substring(messageKeyExt, 96, 32);
        return new ActorBoxKey(aesCipherKey, aesMacKey, kuzCipherKey, kuzMacKey);
    }
}
