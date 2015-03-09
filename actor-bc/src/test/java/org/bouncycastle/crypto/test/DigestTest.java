package org.bouncycastle.crypto.test;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.EncodableDigest;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.test.SimpleTest;

public abstract class DigestTest
    extends SimpleTest
{
    private Digest digest;
    private String[] input;
    private String[] results;

    DigestTest(
        Digest digest,
        String[] input,
        String[] results)
    {
        this.digest = digest;
        this.input = input;
        this.results = results;
    }
    
    public String getName()
    {
        return digest.getAlgorithmName();
    }
    
    public void performTest()
    {
        byte[] resBuf = new byte[digest.getDigestSize()];
    
        for (int i = 0; i < input.length - 1; i++)
        {
            byte[] m = toByteArray(input[i]);
            
            vectorTest(digest, i, resBuf, m, Hex.decode(results[i]));
        }
        
        byte[] lastV = toByteArray(input[input.length - 1]);
        byte[] lastDigest = Hex.decode(results[input.length - 1]);
        
        vectorTest(digest, input.length - 1, resBuf, lastV, Hex.decode(results[input.length - 1]));

        testClone(resBuf, lastV, lastDigest);
        testMemo(resBuf, lastV, lastDigest);
        if (digest instanceof EncodableDigest)
        {
            testEncodedState(resBuf, lastV, lastDigest);
        }
    }

    private void testEncodedState(byte[] resBuf, byte[] input, byte[] expected)
    {
        // test state encoding;
        digest.update(input, 0, input.length / 2);

        // copy the Digest
        Digest copy1 = cloneDigest(((EncodableDigest)digest).getEncodedState());
        Digest copy2 = cloneDigest(((EncodableDigest)copy1).getEncodedState());

        digest.update(input, input.length / 2, input.length - input.length / 2);

        digest.doFinal(resBuf, 0);

        if (!areEqual(expected, resBuf))
        {
            fail("failing state vector test", expected, new String(Hex.encode(resBuf)));
        }

        copy1.update(input, input.length / 2, input.length - input.length / 2);
        copy1.doFinal(resBuf, 0);

        if (!areEqual(expected, resBuf))
        {
            fail("failing state copy1 vector test", expected, new String(Hex.encode(resBuf)));
        }

        copy2.update(input, input.length / 2, input.length - input.length / 2);
        copy2.doFinal(resBuf, 0);

        if (!areEqual(expected, resBuf))
        {
            fail("failing state copy2 vector test", expected, new String(Hex.encode(resBuf)));
        }
    }

    private void testMemo(byte[] resBuf, byte[] input, byte[] expected)
    {
        Memoable m = (Memoable)digest;

        digest.update(input, 0, input.length/2);

        // copy the Digest
        Memoable copy1 = m.copy();
        Memoable copy2 = copy1.copy();

        digest.update(input, input.length/2, input.length - input.length/2);
        digest.doFinal(resBuf, 0);

        if (!areEqual(expected, resBuf))
        {
            fail("failing memo vector test", results[results.length - 1], new String(Hex.encode(resBuf)));
        }

        m.reset(copy1);

        digest.update(input, input.length/2, input.length - input.length/2);
        digest.doFinal(resBuf, 0);

        if (!areEqual(expected, resBuf))
        {
            fail("failing memo reset vector test", results[results.length - 1], new String(Hex.encode(resBuf)));
        }

        Digest md = (Digest)copy2;

        md.update(input, input.length/2, input.length - input.length/2);
        md.doFinal(resBuf, 0);

        if (!areEqual(expected, resBuf))
        {
            fail("failing memo copy vector test", results[results.length - 1], new String(Hex.encode(resBuf)));
        }
    }

    private void testClone(byte[] resBuf, byte[] input, byte[] expected)
    {
        digest.update(input, 0, input.length/2);

        // clone the Digest
        Digest d = cloneDigest(digest);

        digest.update(input, input.length/2, input.length - input.length/2);
        digest.doFinal(resBuf, 0);

        if (!areEqual(expected, resBuf))
        {
            fail("failing clone vector test", results[results.length - 1], new String(Hex.encode(resBuf)));
        }

        d.update(input, input.length/2, input.length - input.length/2);
        d.doFinal(resBuf, 0);

        if (!areEqual(expected, resBuf))
        {
            fail("failing second clone vector test", results[results.length - 1], new String(Hex.encode(resBuf)));
        }
    }

    protected byte[] toByteArray(String input)
    {
        byte[] bytes = new byte[input.length()];
        
        for (int i = 0; i != bytes.length; i++)
        {
            bytes[i] = (byte)input.charAt(i);
        }
        
        return bytes;
    }
    
    private void vectorTest(
        Digest digest,
        int count,
        byte[] resBuf,
        byte[] input,
        byte[] expected)
    {
        digest.update(input, 0, input.length);
        digest.doFinal(resBuf, 0);

        if (!areEqual(resBuf, expected))
        {
            fail("Vector " + count + " failed got " + new String(Hex.encode(resBuf)));
        }
    }
    
    protected abstract Digest cloneDigest(Digest digest);

    protected Digest cloneDigest(byte[] encodedState)
    {
        throw new IllegalStateException("Unsupported");
    }

    //
    // optional tests
    //
    protected void millionATest(
        String expected)
    {
        byte[] resBuf = new byte[digest.getDigestSize()];
        
        for (int i = 0; i < 1000000; i++)
        {
            digest.update((byte)'a');
        }
        
        digest.doFinal(resBuf, 0);

        if (!areEqual(resBuf, Hex.decode(expected)))
        {
            fail("Million a's failed", expected, new String(Hex.encode(resBuf)));
        }
    }
    
    protected void sixtyFourKTest(
        String expected)
    {
        byte[] resBuf = new byte[digest.getDigestSize()];
        
        for (int i = 0; i < 65536; i++)
        {
            digest.update((byte)(i & 0xff));
        }
        
        digest.doFinal(resBuf, 0);

        if (!areEqual(resBuf, Hex.decode(expected)))
        {
            fail("64k test failed", expected, new String(Hex.encode(resBuf)));
        }
    }
}
