package org.bouncycastle.crypto.test;

import org.junit.Test;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class AllTests {
    @Test
    public void testMD5() throws Exception {
        new MD5DigestTest().performTest();
    }

    @Test
    public void testSHA1() throws Exception {
        new SHA1DigestTest().performTest();
    }

    @Test
    public void testSHA256() throws Exception {
        new SHA256DigestTest().performTest();
    }

    @Test
    public void testSHA512() throws Exception {
        new SHA512DigestTest().performTest();
    }
}
