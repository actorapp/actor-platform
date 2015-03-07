package im.actor.model.crypto.bouncycastle;

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import im.actor.model.CryptoProvider;
import im.actor.model.crypto.AesCipher;
import im.actor.model.crypto.CryptoKeyPair;
import im.actor.model.crypto.RsaCipher;
import im.actor.model.crypto.RsaEncryptCipher;

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
        byte[] publicKey;
        try {
            RSAKeyParameters rsaPublicKey = (RSAKeyParameters) res.getPublic();
            SubjectPublicKeyInfo info =
                    new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE),
                            new RSAPublicKey(rsaPublicKey.getModulus(), rsaPublicKey.getExponent()));
            publicKey = info.getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Building PKCS#8 key
        RSAPrivateCrtKeyParameters parameter = (RSAPrivateCrtKeyParameters) res.getPrivate();

        org.bouncycastle.asn1.pkcs.RSAPrivateKey pksPrivateKey =
                new org.bouncycastle.asn1.pkcs.RSAPrivateKey(parameter.getModulus(), ZERO, parameter.getExponent(),
                        ZERO, ZERO, ZERO, ZERO, ZERO);

        byte[] privateKey;
        try {
            PrivateKeyInfo info = new PrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE),
                    pksPrivateKey.toASN1Primitive());
            privateKey = info.getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

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
