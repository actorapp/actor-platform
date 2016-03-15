package im.actor.runtime.crypto.ratchet;

import im.actor.runtime.Crypto;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.Digest;
import im.actor.runtime.crypto.primitives.util.ByteStrings;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class RatchetMasterSecret {
    public static byte[] calculateMasterSecret(
            RatchetPrivateKey ownIdentity,
            RatchetPrivateKey ownEphermal,
            RatchetPublicKey foreignIdentity,
            RatchetPublicKey foreignEphermal) {

        byte[] ecResult;
        if (ownIdentity.isBigger(foreignIdentity.getKey())) {
            ecResult =
                    ByteStrings.merge(
                            Curve25519.calculateAgreement(
                                    ownIdentity.getPrivateKey(),
                                    foreignEphermal.getKey()),
                            Curve25519.calculateAgreement(
                                    ownEphermal.getPrivateKey(),
                                    foreignIdentity.getKey()),
                            Curve25519.calculateAgreement(
                                    ownEphermal.getPrivateKey(),
                                    foreignEphermal.getKey())
                    );
        } else {
            ecResult =
                    ByteStrings.merge(
                            Curve25519.calculateAgreement(
                                    ownEphermal.getPrivateKey(),
                                    foreignIdentity.getKey()),
                            Curve25519.calculateAgreement(
                                    ownIdentity.getPrivateKey(),
                                    foreignEphermal.getKey()),
                            Curve25519.calculateAgreement(
                                    ownEphermal.getPrivateKey(),
                                    foreignEphermal.getKey())
                    );
        }
        Digest sha256 = Crypto.createSHA256();
        sha256.update(ecResult, 0, ecResult.length);
        byte[] res = new byte[32];
        sha256.doFinal(res, 0);
        return res;
    }
}
