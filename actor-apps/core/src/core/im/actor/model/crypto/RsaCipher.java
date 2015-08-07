/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.crypto;

public interface RsaCipher extends RsaEncryptCipher {

    byte[] decrypt(byte[] sourceData);
}