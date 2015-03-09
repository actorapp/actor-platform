package im.actor.model.crypto;

import org.junit.Test;

import im.actor.model.crypto.asn1.ASN1;
import im.actor.model.crypto.asn1.ASN1BitString;
import im.actor.model.crypto.encoding.X509RsaPublicKey;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by ex3ndr on 09.03.15.
 */
public class Asn1Test {

    private static final String DEMO_KEY1 = "30819d300d06092a864886f70d010101050003818b00308187028181008e98c4695531771914d6b0cf5e993b3c4883616a03896904143401b4196817fa8eee90e8a824b36279c8b6f3132624f65955ac7dffbd67c354c5447afd8d29b755004adcd265ff55b0131ae3f3280036981575949f978f3eaef8ca19b3b7b97d2a3326c1e411c43d9d1fc3d87dc0ab3f657d5b57664367e0f85d88dde8844737020103";
    private static final String DEMO_KEY2 = "30819d300d06092a864886f70d010101050003818b0030818702818100ba741a5d26ff2c9391d7a7b5d6223e11e07a47e4c68fa444d1d48f7fe9a9c19ad22885c6d1c910008cdd3f3783c563e97ae510cfa32acd8a44e5ce1e587d9e736acf7b0dae0613014040e0f26084529a12975effc7168d47d4e1244f6290bc0bb6d86e4ad84d16b7cc90aa151fece4cba1b05a2e771e16574395b135136da777020103";

    @Test
    public void testASN1() throws Exception {
        String data = "asdaksjd ajsdbasdkakjsdadfsgdfgksadsf";
        byte[] serialized = new ASN1BitString(0, data.getBytes("UTF-8")).serialize();
        String data2 = new String(((ASN1BitString) ASN1.readObject(serialized)).getContent(), "UTF-8");
        assertEquals(data, data2);
    }

    @Test
    public void testPublicKey() throws Exception {
        byte[] srcKey = CryptoUtils.fromHex(DEMO_KEY1);
        X509RsaPublicKey publicKey = new X509RsaPublicKey(srcKey);
        assertArrayEquals(srcKey, publicKey.serialize());

        srcKey = CryptoUtils.fromHex(DEMO_KEY2);
        publicKey = new X509RsaPublicKey(srcKey);
        assertArrayEquals(srcKey, publicKey.serialize());
    }
}
