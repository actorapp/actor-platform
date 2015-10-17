package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

/**
 * base implementation of MD4 family style digest as outlined in
 * "Handbook of Applied Cryptography", pages 344 - 347.
 */
public abstract class GeneralDigest
    implements ExtendedDigest, Memoable
{
    private static final int BYTE_LENGTH = 64;

    private final byte[]  xBuf = new byte[4];
    private int           xBufOff;

    private long    byteCount;

    /**
     * Standard constructor
     */
    protected GeneralDigest()
    {
        xBufOff = 0;
    }

    /**
     * Copy constructor.  We are using copy constructors in place
     * of the Object.clone() interface as this interface is not
     * supported by J2ME.
     */
    protected GeneralDigest(GeneralDigest t)
    {
        copyIn(t);
    }

    protected GeneralDigest(byte[] encodedState)
    {
        System.arraycopy(encodedState, 0, xBuf, 0, xBuf.length);
        xBufOff = Pack.bigEndianToInt(encodedState, 4);
        byteCount = Pack.bigEndianToLong(encodedState, 8);
    }

    protected void copyIn(GeneralDigest t)
    {
        System.arraycopy(t.xBuf, 0, xBuf, 0, t.xBuf.length);

        xBufOff = t.xBufOff;
        byteCount = t.byteCount;
    }

    public void update(
        byte in)
    {
        xBuf[xBufOff++] = in;

        if (xBufOff == xBuf.length)
        {
            processWord(xBuf, 0);
            xBufOff = 0;
        }

        byteCount++;
    }

    public void update(
        byte[]  in,
        int     inOff,
        int     len)
    {
        //
        // fill the current word
        //
        while ((xBufOff != 0) && (len > 0))
        {
            update(in[inOff]);

            inOff++;
            len--;
        }

        //
        // process whole words.
        //
        while (len > xBuf.length)
        {
            processWord(in, inOff);

            inOff += xBuf.length;
            len -= xBuf.length;
            byteCount += xBuf.length;
        }

        //
        // load in the remainder.
        //
        while (len > 0)
        {
            update(in[inOff]);

            inOff++;
            len--;
        }
    }

    public void finish()
    {
        long    bitLength = (byteCount << 3);

        //
        // add the pad bytes.
        //
        update((byte)128);

        while (xBufOff != 0)
        {
            update((byte)0);
        }

        processLength(bitLength);

        processBlock();
    }

    public void reset()
    {
        byteCount = 0;

        xBufOff = 0;
        for (int i = 0; i < xBuf.length; i++)
        {
            xBuf[i] = 0;
        }
    }

    protected void populateState(byte[] state)
    {
        System.arraycopy(xBuf, 0, state, 0, xBufOff);
        Pack.intToBigEndian(xBufOff, state, 4);
        Pack.longToBigEndian(byteCount, state, 8);
    }

    public int getByteLength()
    {
        return BYTE_LENGTH;
    }
    
    protected abstract void processWord(byte[] in, int inOff);

    protected abstract void processLength(long bitLength);

    protected abstract void processBlock();
}
