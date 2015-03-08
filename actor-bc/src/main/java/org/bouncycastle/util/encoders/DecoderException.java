package org.bouncycastle.util.encoders;

/**
 * Exception thrown if an attempt is made to decode invalid data, or some other failure occurs.
 */
public class DecoderException
    extends IllegalStateException
{
    private Throwable cause;

    DecoderException(String msg, Throwable cause)
    {
        super(msg);

        this.cause = cause;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
