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

/**
 * A tuple that contains a Curve25519 public and private key.
 *
 * @author Moxie Marlinspike
 */
public class Curve25519KeyPair {

  private final byte[] publicKey;
  private final byte[] privateKey;

  Curve25519KeyPair(byte[] publicKey, byte[] privateKey) {
    this.publicKey  = publicKey;
    this.privateKey = privateKey;
  }

  /**
   * @return The Curve25519 public key.
   */
  public byte[] getPublicKey() {
    return publicKey;
  }

  /**
   * @return The Curve25519 private key.
   */
  public byte[] getPrivateKey() {
    return privateKey;
  }
}
