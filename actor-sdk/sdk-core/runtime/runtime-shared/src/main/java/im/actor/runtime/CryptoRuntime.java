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

    /**
     * Calculate SHA256
     *
     * @param data source data
     * @return sha256 hash of data
     */
    @ObjectiveCName("SHA256WithData:")
    byte[] SHA256(byte[] data);

    /**
     * Calculate SHA512
     *
     * @param data source data
     * @return sha512 hash of data
     */
    @ObjectiveCName("SHA512WithData:")
    byte[] SHA512(byte[] data);
}