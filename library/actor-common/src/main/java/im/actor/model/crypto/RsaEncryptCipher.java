/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.crypto;

public interface RsaEncryptCipher {
    byte[] encrypt(byte[] sourceData);
}
