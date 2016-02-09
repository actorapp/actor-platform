package im.actor.crypto.ratchet;

import im.actor.crypto.box.ActorBoxKey;
import im.actor.crypto.primitives.digest.SHA256;
import im.actor.crypto.primitives.hmac.HMAC;
import im.actor.crypto.primitives.kdf.HKDF;
import im.actor.crypto.primitives.util.ByteStrings;

public class RatchetMessageKey {
    public static ActorBoxKey buildKey(byte[] rootChainKey, int index) {
        HMAC hmac = new HMAC(rootChainKey, new SHA256());
        byte[] indx = ByteStrings.intToBytes(index);
        hmac.update(indx, 0, indx.length);
        byte[] messageKey = new byte[32];
        hmac.doFinal(messageKey, 0);
        byte[] messageKeyExt = new HKDF(new SHA256()).deriveSecrets(messageKey, 128);
        byte[] aesCipherKey = ByteStrings.substring(messageKeyExt, 0, 32);
        byte[] aesMacKey = ByteStrings.substring(messageKeyExt, 32, 32);
        byte[] kuzCipherKey = ByteStrings.substring(messageKeyExt, 64, 32);
        byte[] kuzMacKey = ByteStrings.substring(messageKeyExt, 96, 32);
        return new ActorBoxKey(aesCipherKey, aesMacKey, kuzCipherKey, kuzMacKey);
    }
}
