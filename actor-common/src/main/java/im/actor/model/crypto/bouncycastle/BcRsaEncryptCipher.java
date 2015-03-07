package im.actor.model.crypto.bouncycastle;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PublicKeyFactory;

import im.actor.model.crypto.RsaEncryptCipher;

/**
 * Created by ex3ndr on 07.03.15.
 */
public class BcRsaEncryptCipher implements RsaEncryptCipher {

    private AsymmetricBlockCipher cipher;

    public BcRsaEncryptCipher(byte[] publicKey) {
        try {
            SubjectPublicKeyInfo publicKeyInfo = new SubjectPublicKeyInfo(ASN1Sequence.getInstance(publicKey));
            AsymmetricKeyParameter param;
            param = PublicKeyFactory.createKey(publicKeyInfo);
            cipher = new OAEPEncoding(new RSAEngine(), new SHA1Digest());
            cipher.init(true, param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized byte[] encrypt(byte[] sourceData) {
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