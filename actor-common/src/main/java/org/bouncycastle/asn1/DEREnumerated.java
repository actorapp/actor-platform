package org.bouncycastle.asn1;

import java.math.BigInteger;

/**
 * @deprecated Use ASN1Enumerated instead of this.
 */
public class DEREnumerated
    extends ASN1Enumerated
{
    /**
     * @param bytes the value of this enumerated as an encoded BigInteger (signed).
     * @deprecated use ASN1Enumerated
     */
    DEREnumerated(byte[] bytes)
    {
        super(bytes);
    }

    /**
     * @param value the value of this enumerated.
     * @deprecated use ASN1Enumerated
     */
    public DEREnumerated(BigInteger value)
    {
        super(value);
    }

    /**
     * @param value the value of this enumerated.
     * @deprecated use ASN1Enumerated
     */
    public DEREnumerated(int value)
    {
        super(value);
    }
}
