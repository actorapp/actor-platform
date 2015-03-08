package org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * ASN.1 <code>SEQUENCE</code> and <code>SEQUENCE OF</code> constructs.
 * <p>
 * DER form is always definite form length fields, while
 * BER support uses indefinite form.
 * <hr>
 * <p><b>X.690</b></p>
 * <p><b>8: Basic encoding rules</b></p>
 * <p><b>8.9 Encoding of a sequence value </b></p>
 * 8.9.1 The encoding of a sequence value shall be constructed.
 * <p>
 * <b>8.9.2</b> The contents octets shall consist of the complete
 * encoding of one data value from each of the types listed in
 * the ASN.1 definition of the sequence type, in the order of
 * their appearance in the definition, unless the type was referenced
 * with the keyword <b>OPTIONAL</b> or the keyword <b>DEFAULT</b>.
 * </p><p>
 * <b>8.9.3</b> The encoding of a data value may, but need not,
 * be present for a type which was referenced with the keyword
 * <b>OPTIONAL</b> or the keyword <b>DEFAULT</b>.
 * If present, it shall appear in the encoding at the point
 * corresponding to the appearance of the type in the ASN.1 definition.
 * </p><p>
 * <b>8.10 Encoding of a sequence-of value </b>
 * </p><p>
 * <b>8.10.1</b> The encoding of a sequence-of value shall be constructed.
 * <p>
 * <b>8.10.2</b> The contents octets shall consist of zero,
 * one or more complete encodings of data values from the type listed in
 * the ASN.1 definition.
 * <p>
 * <b>8.10.3</b> The order of the encodings of the data values shall be
 * the same as the order of the data values in the sequence-of value to
 * be encoded.
 * </p>
 * <p><b>9: Canonical encoding rules</b></p>
 * <p><b>9.1 Length forms</b></p>
 * If the encoding is constructed, it shall employ the indefinite length form.
 * If the encoding is primitive, it shall include the fewest length octets necessary.
 * [Contrast with 8.1.3.2 b).]
 *
 * <p><b>11: Restrictions on BER employed by both CER and DER</b></p>
 * <p><b>11.5 Set and sequence components with default value</b></p>
 * The encoding of a set value or sequence value shall not include
 * an encoding for any component value which is equal to
 * its default value.
 */
public abstract class ASN1Sequence
    extends ASN1Primitive
{
    protected Vector seq = new Vector();

    /**
     * Return an ASN1Sequence from the given object.
     *
     * @param obj the object we want converted.
     * @exception IllegalArgumentException if the object cannot be converted.
     * @return an ASN1Sequence instance, or null.
     */
    public static ASN1Sequence getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof ASN1Sequence)
        {
            return (ASN1Sequence)obj;
        }
        else if (obj instanceof ASN1SequenceParser)
        {
            return ASN1Sequence.getInstance(((ASN1SequenceParser)obj).toASN1Primitive());
        }
        else if (obj instanceof byte[])
        {
            try
            {
                return ASN1Sequence.getInstance(fromByteArray((byte[])obj));
            }
            catch (IOException e)
            {
                throw new IllegalArgumentException("failed to construct sequence from byte[]: " + e.getMessage());
            }
        }
        else if (obj instanceof ASN1Encodable)
        {
            ASN1Primitive primitive = ((ASN1Encodable)obj).toASN1Primitive();

            if (primitive instanceof ASN1Sequence)
            {
                return (ASN1Sequence)primitive;
            }
        }

        throw new IllegalArgumentException("unknown object in getInstance: " + obj.getClass().getName());
    }

    /**
     * Return an ASN1 sequence from a tagged object. There is a special
     * case here, if an object appears to have been explicitly tagged on 
     * reading but we were expecting it to be implicitly tagged in the 
     * normal course of events it indicates that we lost the surrounding
     * sequence - so we need to add it back (this will happen if the tagged
     * object is a sequence that contains other sequences). If you are
     * dealing with implicitly tagged sequences you really <b>should</b>
     * be using this method.
     *
     * @param obj the tagged object.
     * @param explicit true if the object is meant to be explicitly tagged,
     *          false otherwise.
     * @exception IllegalArgumentException if the tagged object cannot
     *          be converted.
     * @return an ASN1Sequence instance.
     */
    public static ASN1Sequence getInstance(
        ASN1TaggedObject    obj,
        boolean             explicit)
    {
        if (explicit)
        {
            if (!obj.isExplicit())
            {
                throw new IllegalArgumentException("object implicit - explicit expected.");
            }

            return ASN1Sequence.getInstance(obj.getObject().toASN1Primitive());
        }
        else
        {
            //
            // constructed object which appears to be explicitly tagged
            // when it should be implicit means we have to add the
            // surrounding sequence.
            //
            if (obj.isExplicit())
            {
                if (obj instanceof BERTaggedObject)
                {
                    return new BERSequence(obj.getObject());
                }
                else
                {
                    return new DLSequence(obj.getObject());
                }
            }
            else
            {
                if (obj.getObject() instanceof ASN1Sequence)
                {
                    return (ASN1Sequence)obj.getObject();
                }
            }
        }

        throw new IllegalArgumentException("unknown object in getInstance: " + obj.getClass().getName());
    }

    /**
     * Create an empty sequence
     */
    protected ASN1Sequence()
    {
    }

    /**
     * Create a sequence containing one object
     * @param obj the object to be put in the SEQUENCE.
     */
    protected ASN1Sequence(
        ASN1Encodable obj)
    {
        seq.addElement(obj);
    }

    /**
     * Create a sequence containing a vector of objects.
     * @param v the vector of objects to be put in the SEQUENCE
     */
    protected ASN1Sequence(
        ASN1EncodableVector v)
    {
        for (int i = 0; i != v.size(); i++)
        {
            seq.addElement(v.get(i));
        }
    }

    /**
     * Create a sequence containing a vector of objects.
     */
    protected ASN1Sequence(
        ASN1Encodable[]   array)
    {
        for (int i = 0; i != array.length; i++)
        {
            seq.addElement(array[i]);
        }
    }

    public ASN1Encodable[] toArray()
    {
        ASN1Encodable[] values = new ASN1Encodable[this.size()];

        for (int i = 0; i != this.size(); i++)
        {
            values[i] = this.getObjectAt(i);
        }

        return values;
    }

    public Enumeration getObjects()
    {
        return seq.elements();
    }

    public ASN1SequenceParser parser()
    {
        final ASN1Sequence outer = this;

        return new ASN1SequenceParser()
        {
            private final int max = size();

            private int index;

            public ASN1Encodable readObject() throws IOException
            {
                if (index == max)
                {
                    return null;
                }
                
                ASN1Encodable obj = getObjectAt(index++);
                if (obj instanceof ASN1Sequence)
                {
                    return ((ASN1Sequence)obj).parser();
                }
                if (obj instanceof ASN1Set)
                {
                    return ((ASN1Set)obj).parser();
                }

                return obj;
            }

            public ASN1Primitive getLoadedObject()
            {
                return outer;
            }
            
            public ASN1Primitive toASN1Primitive()
            {
                return outer;
            }
        };
    }

    /**
     * Return the object at the sequence position indicated by index.
     *
     * @param index the sequence number (starting at zero) of the object
     * @return the object at the sequence position indicated by index.
     */
    public ASN1Encodable getObjectAt(
        int index)
    {
        return (ASN1Encodable)seq.elementAt(index);
    }

    /**
     * Return the number of objects in this sequence.
     *
     * @return the number of objects in this sequence.
     */
    public int size()
    {
        return seq.size();
    }

    public int hashCode()
    {
        Enumeration             e = this.getObjects();
        int                     hashCode = size();

        while (e.hasMoreElements())
        {
            Object o = getNext(e);
            hashCode *= 17;

            hashCode ^= o.hashCode();
        }

        return hashCode;
    }

    boolean asn1Equals(
        ASN1Primitive o)
    {
        if (!(o instanceof ASN1Sequence))
        {
            return false;
        }
        
        ASN1Sequence   other = (ASN1Sequence)o;

        if (this.size() != other.size())
        {
            return false;
        }

        Enumeration s1 = this.getObjects();
        Enumeration s2 = other.getObjects();

        while (s1.hasMoreElements())
        {
            ASN1Encodable obj1 = getNext(s1);
            ASN1Encodable obj2 = getNext(s2);

            ASN1Primitive o1 = obj1.toASN1Primitive();
            ASN1Primitive o2 = obj2.toASN1Primitive();

            if (o1 == o2 || o1.equals(o2))
            {
                continue;
            }

            return false;
        }

        return true;
    }

    private ASN1Encodable getNext(Enumeration e)
    {
        ASN1Encodable encObj = (ASN1Encodable)e.nextElement();

        return encObj;
    }

    /**
     * Change current SEQUENCE object to be encoded as {@link DERSequence}.
     * This is part of Distinguished Encoding Rules form serialization.
     */
    ASN1Primitive toDERObject()
    {
        ASN1Sequence derSeq = new DERSequence();

        derSeq.seq = this.seq;

        return derSeq;
    }

    /**
     * Change current SEQUENCE object to be encoded as {@link DLSequence}.
     * This is part of Direct Length form serialization.
     */
    ASN1Primitive toDLObject()
    {
        ASN1Sequence dlSeq = new DLSequence();

        dlSeq.seq = this.seq;

        return dlSeq;
    }

    boolean isConstructed()
    {
        return true;
    }

    abstract void encode(ASN1OutputStream out)
        throws IOException;

    public String toString() 
    {
        return seq.toString();
    }
}
