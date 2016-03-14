package im.actor.runtime.crypto.ratchet;

import im.actor.runtime.Crypto;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.primitives.kdf.HKDF;

public class RatchetRootChainKey {
    public static byte[] makeRootChainKey(RatchetPrivateKey ownEphermal,
                                          RatchetPublicKey theirEphermal,
                                          byte[] masterSecret) {

        byte[] ecRes = Curve25519.calculateAgreement(ownEphermal.getPrivateKey(),
                theirEphermal.getKey());

        HKDF hkdf = new HKDF(Crypto.createSHA256());
        return hkdf.deriveSecrets(ecRes, masterSecret, "ActorRatchet".getBytes(), 32);
    }
}
