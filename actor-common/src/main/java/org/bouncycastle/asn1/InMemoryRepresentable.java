package org.bouncycastle.asn1;

import java.io.IOException;

/**
 * Interface implemented by objects that can be converted from streaming to in-memory objects.
 */
public interface InMemoryRepresentable
{
    /**
     * Get the in-memory representation of the ASN.1 object.
     * @throws IOException for bad input data.
     */
    ASN1Primitive getLoadedObject()
        throws IOException;
}
