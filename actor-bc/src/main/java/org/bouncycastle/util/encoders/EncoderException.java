package org.bouncycastle.util.encoders;

/**
 * Exception thrown if an attempt is made to encode invalid data, or some other failure occurs.
 */
public class EncoderException
    extends IllegalStateException
{
    private Throwable cause;

    EncoderException(String msg, Throwable cause)
    {
        super(msg);

        this.cause = cause;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
