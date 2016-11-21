/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android;

import im.actor.runtime.android.crypto.AndroidSHA256;
import im.actor.runtime.crypto.Digest;
import im.actor.runtime.generic.GenericCryptoProvider;

public class AndroidCryptoProvider extends GenericCryptoProvider {

    @Override
    public Digest SHA256() {
        return new AndroidSHA256();
    }
}
