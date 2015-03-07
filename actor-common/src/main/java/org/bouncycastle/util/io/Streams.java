package org.bouncycastle.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility methods to assist with stream processing.
 */
public final class Streams
{
    private static int BUFFER_SIZE = 512;

    /**
     * Read stream till EOF is encountered.
     *
     * @param inStr stream to be emptied.
     * @throws IOException in case of underlying IOException.
     */
    public static void drain(InputStream inStr)
        throws IOException
    {
        byte[] bs = new byte[BUFFER_SIZE];
        while (inStr.read(bs, 0, bs.length) >= 0)
        {
        }
    }

    /**
     * Read stream fully, returning contents in a byte array.
     *
     * @param inStr stream to be read.
     * @return a byte array representing the contents of inStr.
     * @throws IOException in case of underlying IOException.
     */
    public static byte[] readAll(InputStream inStr)
        throws IOException
    {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        pipeAll(inStr, buf);
        return buf.toByteArray();
    }

    /**
     * Read from inStr up to a maximum number of bytes, throwing an exception if more the maximum amount
     * of requested data is available.
     *
     * @param inStr stream to be read.
     * @param limit maximum number of bytes that can be read.
     * @return a byte array representing the contents of inStr.
     * @throws IOException in case of underlying IOException, or if limit is reached on inStr still has data in it.
     */
    public static byte[] readAllLimited(InputStream inStr, int limit)
        throws IOException
    {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        pipeAllLimited(inStr, limit, buf);
        return buf.toByteArray();
    }

    /**
     * Fully read in buf's length in data, or up to EOF, whichever occurs first,
     *
     * @param inStr the stream to be read.
     * @param buf the buffer to be read into.
     * @return the number of bytes read into the buffer.
     * @throws IOException in case of underlying IOException.
     */
    public static int readFully(InputStream inStr, byte[] buf)
        throws IOException
    {
        return readFully(inStr, buf, 0, buf.length);
    }

    /**
     * Fully read in len's bytes of data into buf, or up to EOF, whichever occurs first,
     *
     * @param inStr the stream to be read.
     * @param buf the buffer to be read into.
     * @param off offset into buf to start putting bytes into.
     * @param len  the number of bytes to be read.
     * @return the number of bytes read into the buffer.
     * @throws IOException in case of underlying IOException.
     */
    public static int readFully(InputStream inStr, byte[] buf, int off, int len)
        throws IOException
    {
        int totalRead = 0;
        while (totalRead < len)
        {
            int numRead = inStr.read(buf, off + totalRead, len - totalRead);
            if (numRead < 0)
            {
                break;
            }
            totalRead += numRead;
        }
        return totalRead;
    }

    /**
     * Write the full contents of inStr to the destination stream outStr.
     *
     * @param inStr source input stream.
     * @param outStr destination output stream.
     * @throws IOException in case of underlying IOException.
     */
    public static void pipeAll(InputStream inStr, OutputStream outStr)
        throws IOException
    {
        byte[] bs = new byte[BUFFER_SIZE];
        int numRead;
        while ((numRead = inStr.read(bs, 0, bs.length)) >= 0)
        {
            outStr.write(bs, 0, numRead);
        }
    }

    /**
     * Write up to limit bytes of data from inStr to the destination stream outStr.
     *
     * @param inStr source input stream.
     * @param limit the maximum number of bytes allowed to be read.
     * @param outStr destination output stream.
     * @throws IOException in case of underlying IOException, or if limit is reached on inStr still has data in it.
     */
    public static long pipeAllLimited(InputStream inStr, long limit, OutputStream outStr)
        throws IOException
    {
        long total = 0;
        byte[] bs = new byte[BUFFER_SIZE];
        int numRead;
        while ((numRead = inStr.read(bs, 0, bs.length)) >= 0)
        {
            total += numRead;
            if (total > limit)
            {
                throw new StreamOverflowException("Data Overflow");
            }
            outStr.write(bs, 0, numRead);
        }
        return total;
    }
}
