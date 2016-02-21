/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

/**
 * Provider for Cryptography support
 */
public interface CryptoRuntime {

    void waitForCryptoLoaded();
}