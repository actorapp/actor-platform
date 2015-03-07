package im.actor.model.crypto.bouncycastle;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;

import im.actor.model.crypto.RsaCipher;

/**
 * Created by ex3ndr on 07.03.15.
 */
public class BcRsaCipher extends BcRsaEncryptCipher implements RsaCipher {
    private AsymmetricBlockCipher cipher;

    public BcRsaCipher(byte[] publicKey, byte[] privateKey) {
        super(publicKey);

        try {
            PrivateKeyInfo info = new PrivateKeyInfo(ASN1Sequence.getInstance(privateKey));
            RSAPrivateCrtKeyParameters privateCrtKeyParameters = (RSAPrivateCrtKeyParameters) PrivateKeyFactory.createKey(info);
            AsymmetricKeyParameter keyParameter = new RSAKeyParameters(true, privateCrtKeyParameters.getModulus(),
                    privateCrtKeyParameters.getExponent());

            cipher = new OAEPEncoding(new RSAEngine(), new SHA1Digest());
            cipher.init(false, keyParameter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized byte[] decrypt(byte[] sourceData) {
        if (cipher == null) {
            return null;
        }

        try {
            return cipher.processBlock(sourceData, 0, sourceData.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
