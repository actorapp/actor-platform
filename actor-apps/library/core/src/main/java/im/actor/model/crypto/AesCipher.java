/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.crypto;

public interface AesCipher {
    byte[] encrypt(byte[] source);

    byte[] decrypt(byte[] source);
}
