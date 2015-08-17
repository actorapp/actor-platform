/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.crypto.AesCipher;
import im.actor.runtime.crypto.CryptoKeyPair;
import im.actor.runtime.crypto.RsaCipher;
import im.actor.runtime.crypto.RsaEncryptCipher;

/**
 * Provider for Cryptography support
 */
public interface CryptoRuntime {

    /**
     * Generation of RSA 1024 bit key pair
     *
     * @return generated key pair
     */
    @ObjectiveCName("generateRSA1024KeyPair")
    CryptoKeyPair generateRSA1024KeyPair();

    /**
     * Create RSA encryption cipher
     *
     * @param publicKey public rsa key
     * @return the RSA encryption cipher for publicKey
     */
    @ObjectiveCName("createRSAOAEPSHA1CipherWithPublicKey:")
    RsaEncryptCipher createRSAOAEPSHA1Cipher(byte[] publicKey);

    /**
     * Create RSA encryption/decryption cipher
     *
     * @param publicKey  public rsa key
     * @param privateKey private rsa key
     * @return the RSA encryption/decryption cipher for key pair
     */
    @ObjectiveCName("createRSAOAEPSHA1CipherWithPublicKey:withPrivateKey:")
    RsaCipher createRSAOAEPSHA1Cipher(byte[] publicKey, byte[] privateKey);

    /**
     * Create AES cipher
     *
     * @param key AES Key
     * @param iv  AES IV
     * @return the AES cipher for keys
     */
    @ObjectiveCName("createAESCBCPKS7CipherWithKey:withIv:")
    AesCipher createAESCBCPKS7Cipher(byte[] key, byte[] iv);

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