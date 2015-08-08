/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.runtime.crypto.bouncycastle;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

import java.math.BigInteger;

import im.actor.core.runtime.CryptoRuntime;
import im.actor.core.crypto.AesCipher;
import im.actor.core.crypto.CryptoKeyPair;
import im.actor.core.crypto.RsaCipher;
import im.actor.core.crypto.RsaEncryptCipher;
import im.actor.core.log.Log;

public class BouncyCastleRuntime implements CryptoRuntime {

    // We expect that public exponent = 3 will be good
    // chose for best performance in web.
    private static final BigInteger RSA_EXPONENT = new BigInteger("3");
    private static final int RSA_CERTAINITY = 80;
    private static final int RSA_1024_STREIGHT = 1024;

    private static final BigInteger ZERO = BigInteger.valueOf(0);

    //private static final RandomProvider RANDOM = new RandomProvider();

    private final RandomProvider provider;

    public BouncyCastleRuntime(RandomProvider provider) {
        this.provider = provider;
    }

    @Override
    public CryptoKeyPair generateRSA1024KeyPair() {
        RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
        generator.init(new RSAKeyGenerationParameters(
                RSA_EXPONENT,
                provider,
                RSA_1024_STREIGHT,
                RSA_CERTAINITY));
        Log.d("RSA", "Starting key generation...");
        AsymmetricCipherKeyPair res = generator.generateKeyPair();

        // Building x.509 public key
        RSAKeyParameters rsaPublicKey = (RSAKeyParameters) res.getPublic();
        byte[] publicKey = new im.actor.core.runtime.crypto.encoding.X509RsaPublicKey(rsaPublicKey.getModulus(),
                rsaPublicKey.getExponent()).serialize();

        // Building PKCS#8 key
        RSAPrivateCrtKeyParameters parameter = (RSAPrivateCrtKeyParameters) res.getPrivate();
        byte[] privateKey = new im.actor.core.runtime.crypto.encoding.PKS8RsaPrivateKey(parameter.getModulus(), parameter.getExponent()).serialize();

        return new CryptoKeyPair(publicKey, privateKey);
    }

    @Override
    public RsaEncryptCipher createRSAOAEPSHA1Cipher(byte[] key) {
        return new BcRsaEncryptCipher(provider, key);
    }

    @Override
    public RsaCipher createRSAOAEPSHA1Cipher(byte[] publicKey, byte[] privateKey) {
        return new BcRsaCipher(provider, publicKey, privateKey);
    }

    @Override
    public AesCipher createAESCBCPKS7Cipher(byte[] key, byte[] iv) {
        return new BcAesCipher(key, iv);
    }

    @Override
    public byte[] MD5(byte[] data) {
        MD5Digest digest = new MD5Digest();
        digest.update(data, 0, data.length);
        byte[] res = new byte[16];
        digest.doFinal(res, 0);
        return res;
    }

    @Override
    public byte[] SHA256(byte[] data) {
        SHA256Digest digest = new SHA256Digest();
        digest.update(data, 0, data.length);
        byte[] res = new byte[32];
        digest.doFinal(res, 0);
        return res;
    }

    @Override
    public byte[] SHA512(byte[] data) {
        SHA512Digest digest = new SHA512Digest();
        digest.update(data, 0, data.length);
        byte[] res = new byte[64];
        digest.doFinal(res, 0);
        return res;
    }

    @Override
    public byte[] randomBytes(int length) {
        return provider.randomBytes(length);
    }

    @Override
    public int randomInt(int maxValue) {
        return provider.randomInt(maxValue);
    }
}
