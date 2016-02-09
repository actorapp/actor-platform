package im.actor.crypto.ratchet;

import im.actor.crypto.Curve25519;
import im.actor.crypto.primitives.digest.SHA256;
import im.actor.crypto.primitives.util.ByteStrings;

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
        SHA256 sha256 = new SHA256();
        sha256.update(ecResult, 0, ecResult.length);
        byte[] res = new byte[32];
        sha256.doFinal(res, 0);
        return res;
    }
}
