package im.actor.model.crypto.bouncycastle;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

import java.math.BigInteger;
import java.security.SecureRandom;

import im.actor.model.CryptoProvider;
import im.actor.model.crypto.AesCipher;
import im.actor.model.crypto.CryptoKeyPair;
import im.actor.model.crypto.RsaCipher;
import im.actor.model.crypto.RsaEncryptCipher;
import im.actor.model.crypto.encoding.PKS8RsaPrivateKey;
import im.actor.model.crypto.encoding.X509RsaPublicKey;

/**
 * Created by ex3ndr on 27.02.15.
 */
public class BouncyCastleProvider implements CryptoProvider {

    // We expect that public exponent = 3 will be good
    // chose for best performance in web.
    private static final BigInteger RSA_EXPONENT = new BigInteger("3");
    private static final int RSA_CERTAINITY = 80;
    private static final int RSA_1024_STREIGHT = 1024;

    private static final BigInteger ZERO = BigInteger.valueOf(0);

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public CryptoKeyPair generateRSA1024KeyPair() {
        RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
        generator.init(new RSAKeyGenerationParameters(
                RSA_EXPONENT,
                new SecureRandom(),
                RSA_1024_STREIGHT,
                RSA_CERTAINITY));

        AsymmetricCipherKeyPair res = generator.generateKeyPair();

        // Building x.509 public key
        RSAKeyParameters rsaPublicKey = (RSAKeyParameters) res.getPublic();
        byte[] publicKey = new X509RsaPublicKey(rsaPublicKey.getModulus(),
                rsaPublicKey.getExponent()).serialize();

        // Building PKCS#8 key
        RSAPrivateCrtKeyParameters parameter = (RSAPrivateCrtKeyParameters) res.getPrivate();
        byte[] privateKey = new PKS8RsaPrivateKey(parameter.getModulus(), parameter.getExponent()).serialize();

        return new CryptoKeyPair(publicKey, privateKey);
    }

    @Override
    public RsaEncryptCipher createRSAOAEPSHA1Cipher(byte[] key) {
        return new BcRsaEncryptCipher(key);
    }

    @Override
    public RsaCipher createRSAOAEPSHA1Cipher(byte[] publicKey, byte[] privateKey) {
        return new BcRsaCipher(publicKey, privateKey);
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
        byte[] res = new byte[length];
        synchronized (RANDOM) {
            RANDOM.nextBytes(res);
        }
        return res;
    }

    @Override
    public int randomInt(int maxValue) {
        synchronized (RANDOM) {
            return RANDOM.nextInt(maxValue);
        }
    }
}
