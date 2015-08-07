package im.actor.model.crypto;

import org.junit.Test;

import im.actor.model.crypto.asn1.ASN1;
import im.actor.model.crypto.asn1.ASN1BitString;
import im.actor.model.crypto.encoding.PKS8RsaPrivateKey;
import im.actor.model.crypto.encoding.X509RsaPublicKey;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by ex3ndr on 09.03.15.
 */
public class Asn1Test {

    private static final String DEMO_KEY1 = "30819d300d06092a864886f70d010101050003818b00308187028181008e98c4695531771914d6b0cf5e993b3c4883616a03896904143401b4196817fa8eee90e8a824b36279c8b6f3132624f65955ac7dffbd67c354c5447afd8d29b755004adcd265ff55b0131ae3f3280036981575949f978f3eaef8ca19b3b7b97d2a3326c1e411c43d9d1fc3d87dc0ab3f657d5b57664367e0f85d88dde8844737020103";
    private static final String DEMO_KEY2 = "30819d300d06092a864886f70d010101050003818b0030818702818100ba741a5d26ff2c9391d7a7b5d6223e11e07a47e4c68fa444d1d48f7fe9a9c19ad22885c6d1c910008cdd3f3783c563e97ae510cfa32acd8a44e5ce1e587d9e736acf7b0dae0613014040e0f26084529a12975effc7168d47d4e1244f6290bc0bb6d86e4ad84d16b7cc90aa151fece4cba1b05a2e771e16574395b135136da777020103";
    private static final String DEMO_KEY3 = "30820122300d06092a864886f70d01010105000382010f003082010a0282010100b48c9db16a892eadb03293b3ed6467d4d41c851efd9f72c77d79e3afaaed0b6a3b4765c9a7d59f4991160469fe742d9ff926c533221efda41dac269cd3c3b76e1ebc52642268420b18dbdf3c6d7667234ef91195fa4c98c36a6d62695489d24ee301781edc22d165269d7597be224552af3080467564a501756243e1c84df1119141666ab118dbd640091f814855c759a00054c502e71aa04b50ef1e989d55afde8c2f5cf9cf348b4bb14fb9123e065ac613f50a9008a6517636f17aeeb6dca0808ac26609db6c92cd0be3be5a907f64caf0131583ebbf6e11be4002167c0b99cfa54766451a39be43ba4b2e826d651d10390a34a31c0693c2c5596d3725b1250203010001";

    private static final String DEMO_PRIVATE_KEY = "30820137020100300d06092a864886f70d0101010500048201213082011d02010002818100c2f4968198109b7d9bac8d5b5bcf483f0251706adcee1d25fd807781cd302e13f0000cb908f8bd32bf7954f87be5b5b83c1a54973c0a55b2a4892200bf980427d7d53ab8e615e2faf3586d79e370b2f2314d35cfb27208485d35be51d7c417a102817556b6ce71e0dc3b239076b38bc6413d13b4b4c511bef23cf24fa43aeb310201000281810081f86456656067a9127308e7928a302a018ba0473df4136ea9004fabde201eb7f5555dd0b0a5d3772a50e350529923d028118dba2806e3cc6db0c155d51002c40e79b4c33e95af2099ba7ca2a7b44f61b0d724c0e0cf38c37f63c52ad6e68e4d153acac18a06f90421146f8de4593487eab66e90c1530ce987fa193f935d7ec3020100020100020100020100020100";

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

        srcKey = CryptoUtils.fromHex(DEMO_KEY3);
        publicKey = new X509RsaPublicKey(srcKey);
        assertArrayEquals(srcKey, publicKey.serialize());
    }

    @Test
    public void testPrivateKey() throws Exception {
        byte[] src = CryptoUtils.fromHex(DEMO_PRIVATE_KEY);
        PKS8RsaPrivateKey pks8RsaPrivateKey = new PKS8RsaPrivateKey(src);
        assertArrayEquals(src, pks8RsaPrivateKey.serialize());
    }
}
