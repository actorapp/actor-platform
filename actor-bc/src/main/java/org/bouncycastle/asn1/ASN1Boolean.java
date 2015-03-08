package org.bouncycastle.asn1;

import org.bouncycastle.util.Arrays;

import java.io.IOException;

/**
 * Public facade of ASN.1 Boolean data.
 * <p>
 * Use following to place a new instance of ASN.1 Boolean in your dataset:
 * </p>
 * <ul>
 * <li> ASN1Boolean.TRUE literal</li>
 * <li> ASN1Boolean.FALSE literal</li>
 * <li> {@link ASN1Boolean#getInstance(boolean) ASN1Boolean.getInstance(boolean)}</li>
 * <li> {@link ASN1Boolean#getInstance(int) ASN1Boolean.getInstance(int)}</li>
 * </ul>
 * </p>
 */
public class ASN1Boolean
    extends ASN1Primitive
{
    private static final byte[] TRUE_VALUE = new byte[] { (byte)0xff };
    private static final byte[] FALSE_VALUE = new byte[] { 0 };

    private byte[]         value;

    public static final ASN1Boolean FALSE = new ASN1Boolean(false);
    public static final ASN1Boolean TRUE  = new ASN1Boolean(true);

    /**
     * return a boolean from the passed in object.
     *
     * @param obj an ASN1Boolean or an object that can be converted into one.
     * @exception IllegalArgumentException if the object cannot be converted.
     * @return an ASN1Boolean instance.
     */
    public static ASN1Boolean getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof ASN1Boolean)
        {
            return (ASN1Boolean)obj;
        }

        if (obj instanceof byte[])
        {
            byte[] enc = (byte[])obj;
            try
            {
                return (ASN1Boolean)fromByteArray(enc);
            }
            catch (IOException e)
            {
                throw new IllegalArgumentException("failed to construct boolean from byte[]: " + e.getMessage());
            }
        }

        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    /**
     * return an ASN1Boolean from the passed in boolean.
     * @return an ASN1Boolean instance.
     */
    public static ASN1Boolean getInstance(
        boolean  value)
    {
        return (value ? TRUE : FALSE);
    }

    /**
     * return an ASN1Boolean from the passed in value.
     * @return an ASN1Boolean instance.
     */
    public static ASN1Boolean getInstance(
        int value)
    {
        return (value != 0 ? TRUE : FALSE);
    }

    /**
     * return a Boolean from a tagged object.
     *
     * @param obj the tagged object holding the object we want
     * @param explicit true if the object is meant to be explicitly
     *              tagged false otherwise.
     * @exception IllegalArgumentException if the tagged object cannot
     *               be converted.
     * @return an ASN1Boolean instance.
     */
    public static ASN1Boolean getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        ASN1Primitive o = obj.getObject();

        if (explicit || o instanceof ASN1Boolean)
        {
            return getInstance(o);
        }
        else
        {
            return ASN1Boolean.fromOctetString(((ASN1OctetString)o).getOctets());
        }
    }

    ASN1Boolean(
        byte[] value)
    {
        if (value.length != 1)
        {
            throw new IllegalArgumentException("byte value should have 1 byte in it");
        }

        if (value[0] == 0)
        {
            this.value = FALSE_VALUE;
        }
        else if ((value[0] & 0xff) == 0xff)
        {
            this.value = TRUE_VALUE;
        }
        else
        {
            this.value = Arrays.clone(value);
        }
    }

    /**
     * @deprecated use getInstance(boolean) method.
     * @param value true or false.
     */
    public ASN1Boolean(
        boolean     value)
    {
        this.value = (value) ? TRUE_VALUE : FALSE_VALUE;
    }

    public boolean isTrue()
    {
        return (value[0] != 0);
    }

    boolean isConstructed()
    {
        return false;
    }

    int encodedLength()
    {
        return 3;
    }

    void encode(
        ASN1OutputStream out)
        throws IOException
    {
        out.writeEncoded(BERTags.BOOLEAN, value);
    }

    protected boolean asn1Equals(
        ASN1Primitive  o)
    {
        if (o instanceof ASN1Boolean)
        {
            return (value[0] == ((ASN1Boolean)o).value[0]);
        }

        return false;
    }

    public int hashCode()
    {
        return value[0];
    }


    public String toString()
    {
      return (value[0] != 0) ? "TRUE" : "FALSE";
    }

    static ASN1Boolean fromOctetString(byte[] value)
    {
        if (value.length != 1)
        {
            throw new IllegalArgumentException("BOOLEAN value should have 1 byte in it");
        }

        if (value[0] == 0)
        {
            return FALSE;
        }
        else if ((value[0] & 0xff) == 0xff)
        {
            return TRUE;
        }
        else
        {
            return new ASN1Boolean(value);
        }
    }
}
