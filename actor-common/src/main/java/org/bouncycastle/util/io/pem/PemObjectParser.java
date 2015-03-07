package org.bouncycastle.util.io.pem;

import java.io.IOException;

/**
 * Base interface for parsers to convert PEM objects into specific objects.
 */
public interface PemObjectParser
{
    /**
     * Parse an object out of the PEM object passed in.
     *
     * @param obj the PEM object containing the details for the specific object.
     * @return a specific object represented by the  PEM object.
     * @throws IOException on a parsing error.
     */
    Object parseObject(PemObject obj)
            throws IOException;
}
