package org.bouncycastle.asn1;

import java.io.IOException;

/**
 * A basic parser for a SEQUENCE object
 */
public interface ASN1SequenceParser
    extends ASN1Encodable, InMemoryRepresentable
{
    /**
     * Read the next object from the underlying object representing a SEQUENCE.
     *
     * @throws IOException for bad input stream.
     * @return the next object, null if we are at the end.
     */
    ASN1Encodable readObject()
        throws IOException;
}
