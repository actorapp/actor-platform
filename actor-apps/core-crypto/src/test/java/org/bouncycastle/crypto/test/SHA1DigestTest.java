package org.bouncycastle.crypto.test;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;

/**
 * standard vector test for SHA-1 from "Handbook of Applied Cryptography", page 345.
 */
public class SHA1DigestTest
    extends DigestTest
{
    private static String[] messages =
    {
         "",
         "a",
         "abc",
         "abcdefghijklmnopqrstuvwxyz"
    };
    
    private static String[] digests =
    {
        "da39a3ee5e6b4b0d3255bfef95601890afd80709",
        "86f7e437faa5a7fce15d1ddcb9eaeaea377667b8",
        "a9993e364706816aba3e25717850c26c9cd0d89d",
        "32d10c7b8cf96570ca04ce37f2a19d84240d3a89"
    };
    
    SHA1DigestTest()
    {
        super(new SHA1Digest(), messages, digests);
    }

    protected Digest cloneDigest(Digest digest)
    {
        return new SHA1Digest((SHA1Digest)digest);
    }

    protected Digest cloneDigest(byte[] encodedState)
    {
        return new SHA1Digest(encodedState);
    }

    public static void main(
        String[]    args)
    {
        runTest(new SHA1DigestTest());
    }
}
