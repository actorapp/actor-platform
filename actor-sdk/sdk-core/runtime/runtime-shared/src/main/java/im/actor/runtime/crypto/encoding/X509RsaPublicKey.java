/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto.encoding;

import java.io.IOException;
import java.math.BigInteger;

import im.actor.runtime.crypto.asn1.ASN1;
import im.actor.runtime.crypto.asn1.ASN1BitString;
import im.actor.runtime.crypto.asn1.ASN1Integer;
import im.actor.runtime.crypto.asn1.ASN1Null;
import im.actor.runtime.crypto.asn1.ASN1ObjectIdentifier;
import im.actor.runtime.crypto.asn1.ASN1Primitive;
import im.actor.runtime.crypto.asn1.ASN1Sequence;

public final class X509RsaPublicKey {

    private static final String ALGO_TYPE = "1.2.840.113549.1.1.1";

    private BigInteger modulus;
    private BigInteger exponent;

    public X509RsaPublicKey(BigInteger modulus, BigInteger exponent) {
        this.modulus = modulus;
        this.exponent = exponent;
    }

    public X509RsaPublicKey(byte[] data) throws IOException {
        ASN1Primitive root = ASN1.readObject(data);
        if (!(root instanceof ASN1Sequence)) {
            throw new IOException("Incorrect type of sequence");
        }
        ASN1Sequence rootSequence = (ASN1Sequence) ASN1.readObject(data);

        if (rootSequence.size() != 2) {
            throw new IOException("Incorrect type of sequence");
        }

        if (!(rootSequence.get(0) instanceof ASN1Sequence)) {
            throw new IOException("Incorrect type of sequence");
        }
        if (!(rootSequence.get(1) instanceof ASN1BitString)) {
            throw new IOException("Incorrect type of sequence");
        }

        ASN1Sequence algoHeader = (ASN1Sequence) rootSequence.get(0);
        if (!(algoHeader.get(0) instanceof ASN1ObjectIdentifier)) {
            throw new IOException("Incorrect type of sequence");
        }

        ASN1ObjectIdentifier algo = (ASN1ObjectIdentifier) algoHeader.get(0);
        if (!algo.getIdentifier().equals(ALGO_TYPE)) {
            throw new IOException("Incorrect type of header: " + algo.getIdentifier());
        }

        ASN1BitString bitString = (ASN1BitString) rootSequence.get(1);

        ASN1Primitive keyRoot = ASN1.readObject(bitString.getContent());
        if (!(keyRoot instanceof ASN1Sequence)) {
            throw new IOException("Incorrect type of sequence");
        }

        ASN1Sequence keySequence = (ASN1Sequence) keyRoot;
        if (keySequence.size() != 2) {
            throw new IOException("Incorrect type of sequence");
        }

        if (!(keySequence.get(0) instanceof ASN1Integer)) {
            throw new IOException("Incorrect type of sequence");
        }

        if (!(keySequence.get(1) instanceof ASN1Integer)) {
            throw new IOException("Incorrect type of sequence");
        }

        ASN1Integer modulus = (ASN1Integer) keySequence.get(0);
        ASN1Integer exponent = (ASN1Integer) keySequence.get(1);

        this.modulus = new BigInteger(1, modulus.getData());
        this.exponent = new BigInteger(1, exponent.getData());
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public BigInteger getExponent() {
        return exponent;
    }

    public byte[] serialize(){
        return new ASN1Sequence(
                new ASN1Sequence(
                        new ASN1ObjectIdentifier(ALGO_TYPE),
                        new ASN1Null()),
                new ASN1BitString(0,
                        new ASN1Sequence(
                                new ASN1Integer(modulus),
                                new ASN1Integer(exponent))
                                .serialize())).serialize();
    }
}
