package im.actor.model.crypto;

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by ex3ndr on 07.03.15.
 */

public class RsaTest {

    // We expect that public exponent = 3 will be good
    // chose for best performance in web.
    private static final BigInteger RSA_EXPONENT = new BigInteger("3");

    private static final int RSA_CERTAINITY = 80;
    private static final int RSA_KEY_LENGTH = 1024;

    private static BigInteger ZERO = BigInteger.valueOf(0);

    @org.junit.Test
    public void testKeyParsing() throws Exception {
//        KeyPairGenerator g = null;
//        try {
//            g = KeyPairGenerator.getInstance("RSA");
//        } catch (NoSuchAlgorithmException e) {
//            throw new AssertionError(e);
//        }
//        g.initialize(RSA_KEY_LENGTH);
//        KeyPair keyPair = g.generateKeyPair();
//        PublicKey publicKey = keyPair.getPublic();
//
//        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
//
//        BigInteger publicExponent = rsaPublicKey.getPublicExponent();
//
//        byte[] encodedPublicKey = publicKey.getEncoded();
//
//        ASN1StreamParser parser = new ASN1StreamParser(new ByteArrayInputStream(encodedPublicKey));
//        ASN1Encodable asn1Encodable = parser.readObject();
//        ASN1Primitive primitive = asn1Encodable.toASN1Primitive();
//
//        primitive.toString();

        // Generate RSA key

        RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
        rsaKeyPairGenerator.init(new RSAKeyGenerationParameters(RSA_EXPONENT,
                new SecureRandom(), RSA_KEY_LENGTH, RSA_CERTAINITY));

        AsymmetricCipherKeyPair bcKey = rsaKeyPairGenerator.generateKeyPair();

        RSAPrivateCrtKeyParameters parameter = (RSAPrivateCrtKeyParameters) bcKey.getPrivate();

        org.bouncycastle.asn1.pkcs.RSAPrivateKey pksPrivateKey = new org.bouncycastle.asn1.pkcs.RSAPrivateKey(parameter.getModulus(), ZERO, parameter.getExponent(),
                ZERO, ZERO, ZERO, ZERO, ZERO);

        PrivateKeyInfo info = new PrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE),
                pksPrivateKey.toASN1Primitive());
        byte[] privateKey = info.getEncoded(ASN1Encoding.DER);

//        RSAKeyParameters keyParameter = (RSAKeyParameters) bcKey.getPublic();

//        SubjectPublicKeyInfo info =
//                new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE),
//                        new RSAPublicKey(keyParameter.getModulus(), keyParameter.getExponent()));
//        byte[] encoded = info.getEncoded();
//
//        KeyFactory keyFactory;
//        try {
//            keyFactory = KeyFactory.getInstance("RSA");
//        } catch (NoSuchAlgorithmException e) {
//            throw new AssertionError(e);
//        }
//
//        java.security.interfaces.RSAPublicKey publicKey;
//        try {
//            publicKey = (java.security.interfaces.RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(encoded));
//        } catch (InvalidKeySpecException e) {
//            throw new AssertionError(e);
//        }
//
//        publicKey.toString();
    }
}
