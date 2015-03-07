package org.bouncycastle.util.io;

import java.io.IOException;
import java.io.OutputStream;


/**
 * An output stream which copies anything written into it to another stream.
 */
public class TeeOutputStream
    extends OutputStream
{
    private OutputStream output1;
    private OutputStream output2;

    /**
     * Base constructor.
     *
     * @param output1 the output stream that is wrapped.
     * @param output2 a secondary stream that anything written to output1 is also written to.
     */
    public TeeOutputStream(OutputStream output1, OutputStream output2)
    {
        this.output1 = output1;
        this.output2 = output2;
    }

    public void write(byte[] buf)
        throws IOException
    {
        this.output1.write(buf);
        this.output2.write(buf);
    }

    public void write(byte[] buf, int off, int len)
        throws IOException
    {
        this.output1.write(buf, off, len);
        this.output2.write(buf, off, len);
    }

    public void write(int b)
        throws IOException
    {
        this.output1.write(b);
        this.output2.write(b);
    }

    public void flush()
        throws IOException
    {
        this.output1.flush();
        this.output2.flush();
    }

    public void close()
        throws IOException
    {
        this.output1.close();
        this.output2.close();
    }
}