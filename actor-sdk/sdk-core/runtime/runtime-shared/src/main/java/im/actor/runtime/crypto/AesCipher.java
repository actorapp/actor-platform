/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto;

public interface AesCipher {
    byte[] encrypt(byte[] source);

    byte[] decrypt(byte[] source);
}
