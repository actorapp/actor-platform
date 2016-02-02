/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * Provider for Cryptography support
 */
public interface CryptoRuntime {

    /**
     * Calculate MD5
     *
     * @param data source data
     * @return md5 hash of data
     */
    @ObjectiveCName("MD5WithData:")
    byte[] MD5(byte[] data);

    byte[] fromHex(String hex);

    byte[] fromHexReverse(String hex);

    String toHex(byte[] raw);
}