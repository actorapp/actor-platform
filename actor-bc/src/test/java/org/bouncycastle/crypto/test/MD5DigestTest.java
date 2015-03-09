package org.bouncycastle.crypto.test;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;

/**
 * standard vector test for MD5 from "Handbook of Applied Cryptography", page 345.
 */
public class MD5DigestTest
    extends DigestTest
{
    static final String[] messages =
    {
        "",
        "a",
        "abc",
        "abcdefghijklmnopqrstuvwxyz"
    };
    
    static final String[] digests =
    {
        "d41d8cd98f00b204e9800998ecf8427e",
        "0cc175b9c0f1b6a831c399e269772661",
        "900150983cd24fb0d6963f7d28e17f72",
        "c3fcd3d76192e4007dfb496cca67e13b"
    };

    MD5DigestTest()
    {
        super(new MD5Digest(), messages, digests);
    }

    protected Digest cloneDigest(Digest digest)
    {
        return new MD5Digest((MD5Digest)digest);
    }
    
    public static void main(
        String[]    args)
    {
        runTest(new MD5DigestTest());
    }
}
