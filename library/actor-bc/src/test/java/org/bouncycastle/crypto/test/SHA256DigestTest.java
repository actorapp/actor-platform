package org.bouncycastle.crypto.test;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

/**
 * standard vector test for SHA-256 from FIPS Draft 180-2.
 *
 * Note, the first two vectors are _not_ from the draft, the last three are.
 */
public class SHA256DigestTest
    extends DigestTest
{
    private static String[] messages =
    {
        "",
        "a",
        "abc",
        "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
    };
    
    private static String[] digests =
    {
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb",
        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
        "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1"
    };
    
    // 1 million 'a'
    static private String  million_a_digest = "cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0";

    SHA256DigestTest()
    {
        super(new SHA256Digest(), messages, digests);
    }

    public void performTest()
    {
        super.performTest();

        millionATest(million_a_digest);
    }

    protected Digest cloneDigest(Digest digest)
    {
        return new SHA256Digest((SHA256Digest)digest);
    }

    protected Digest cloneDigest(byte[] encodedState)
    {
        return new SHA256Digest(encodedState);
    }

    public static void main(
        String[]    args)
    {
        runTest(new SHA256DigestTest());
    }
}
