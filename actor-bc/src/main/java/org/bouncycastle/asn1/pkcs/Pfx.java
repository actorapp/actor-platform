package org.bouncycastle.asn1.pkcs;

import org.bouncycastle.asn1.*;

import java.math.BigInteger;

/**
 * the infamous Pfx from PKCS12
 */
public class Pfx
    extends ASN1Object
    implements PKCSObjectIdentifiers
{
    private ContentInfo             contentInfo;
    private MacData                 macData = null;

    private Pfx(
        ASN1Sequence   seq)
    {
        BigInteger  version = ((ASN1Integer)seq.getObjectAt(0)).getValue();
        if (version.intValue() != 3)
        {
            throw new IllegalArgumentException("wrong version for PFX PDU");
        }

        contentInfo = ContentInfo.getInstance(seq.getObjectAt(1));

        if (seq.size() == 3)
        {
            macData = MacData.getInstance(seq.getObjectAt(2));
        }
    }

    public static Pfx getInstance(
        Object  obj)
    {
        if (obj instanceof Pfx)
        {
            return (Pfx)obj;
        }

        if (obj != null)
        {
            return new Pfx(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    public Pfx(
        ContentInfo     contentInfo,
        MacData         macData)
    {
        this.contentInfo = contentInfo;
        this.macData = macData;
    }

    public ContentInfo getAuthSafe()
    {
        return contentInfo;
    }

    public MacData getMacData()
    {
        return macData;
    }

    public ASN1Primitive toASN1Primitive()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(new ASN1Integer(3));
        v.add(contentInfo);

        if (macData != null)
        {
            v.add(macData);
        }

        return new BERSequence(v);
    }
}
