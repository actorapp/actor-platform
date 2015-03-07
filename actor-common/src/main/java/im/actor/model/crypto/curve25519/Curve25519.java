/**
 * Copyright (C) 2015 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package im.actor.model.crypto.curve25519;

import im.actor.model.crypto.CryptoUtils;
import im.actor.model.crypto.curve25519.java.JavaCurve25519Provider;

/**
 * A Curve25519 interface for generating keys, calculating agreements, creating signatures,
 * and verifying signatures.
 *
 * @author Moxie Marlinspike
 */
public class Curve25519 {

    private final Curve25519Provider provider = new JavaCurve25519Provider();

    /**
     * Generates a Curve25519 keypair.
     *
     * @return A randomly generated Curve25519 keypair.
     */
    public Curve25519KeyPair generateKeyPair() {
        byte[] privateKey = provider.generatePrivateKey();
        byte[] publicKey = provider.generatePublicKey(privateKey);

        return new Curve25519KeyPair(publicKey, privateKey);
    }

    /**
     * Calculates an ECDH agreement.
     *
     * @param publicKey  The Curve25519 (typically remote party's) public key.
     * @param privateKey The Curve25519 (typically yours) private key.
     * @return A 32-byte shared secret.
     */
    public byte[] calculateAgreement(byte[] publicKey, byte[] privateKey) {
        return provider.calculateAgreement(privateKey, publicKey);
    }

    /**
     * Calculates a Curve25519 signature.
     *
     * @param privateKey The private Curve25519 key to create the signature with.
     * @param message    The message to sign.
     * @return A 64-byte signature.
     */
    public byte[] calculateSignature(byte[] privateKey, byte[] message) {
        byte[] random = CryptoUtils.randomBytes(64);
        return provider.calculateSignature(random, privateKey, message);
    }

    /**
     * Verify a Curve25519 signature.
     *
     * @param publicKey The Curve25519 public key the signature belongs to.
     * @param message   The message that was signed.
     * @param signature The signature to verify.
     * @return true if valid, false if not.
     */
    public boolean verifySignature(byte[] publicKey, byte[] message, byte[] signature) {
        return provider.verifySignature(publicKey, message, signature);
    }
}
