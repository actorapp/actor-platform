/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto.encoding;

import java.io.IOException;
import java.math.BigInteger;

import im.actor.runtime.crypto.asn1.ASN1;
import im.actor.runtime.crypto.asn1.ASN1Integer;
import im.actor.runtime.crypto.asn1.ASN1Null;
import im.actor.runtime.crypto.asn1.ASN1ObjectIdentifier;
import im.actor.runtime.crypto.asn1.ASN1OctetString;
import im.actor.runtime.crypto.asn1.ASN1Primitive;
import im.actor.runtime.crypto.asn1.ASN1Sequence;

public class PKS8RsaPrivateKey {

    private static final String ALGO = "1.2.840.113549.1.1.1";

    private BigInteger modulus;
    private BigInteger exponent;

    public PKS8RsaPrivateKey(BigInteger modulus, BigInteger exponent) {
        this.modulus = modulus;
        this.exponent = exponent;
    }

    public PKS8RsaPrivateKey(byte[] encoded) throws IOException {
        ASN1Primitive root = ASN1.readObject(encoded);
        if (!(root instanceof ASN1Sequence)) {
            throw new IOException("Incorrect type of sequence");
        }
        ASN1Sequence sequence = (ASN1Sequence) root;

        if (!(sequence.get(0) instanceof ASN1Integer)) {
            throw new IOException("Incorrect type of sequence");
        }
        if (((ASN1Integer) sequence.get(0)).asBigInteger().intValue() != 0) {
            throw new IOException("Incorrect type of sequence");
        }

        if (!(sequence.get(1) instanceof ASN1Sequence)) {
            throw new IOException("Incorrect type of sequence");
        }
        ASN1Sequence algoHeader = (ASN1Sequence) sequence.get(1);
        if (!(algoHeader.get(0) instanceof ASN1ObjectIdentifier)) {
            throw new IOException("Incorrect type of sequence");
        }

        ASN1ObjectIdentifier algo = (ASN1ObjectIdentifier) algoHeader.get(0);
        if (!algo.getIdentifier().equals(ALGO)) {
            throw new IOException("Incorrect type of header: " + algo.getIdentifier());
        }

        if (!(sequence.get(2) instanceof ASN1OctetString)) {
            throw new IOException("Incorrect type of sequence");
        }

        byte[] contents = ((ASN1OctetString) sequence.get(2)).getData();
        ASN1Primitive keyRoot = ASN1.readObject(contents);

        if (!(keyRoot instanceof ASN1Sequence)) {
            throw new IOException("Incorrect type of sequence");
        }

        ASN1Sequence keySequence = (ASN1Sequence) keyRoot;
        if (keySequence.size() != 9) {
            throw new IOException("Incorrect type of sequence");
        }
        for (int i = 0; i < 9; i++) {
            if (!(keySequence.get(i) instanceof ASN1Integer)) {
                throw new IOException("Incorrect type of sequence");
            }
        }

        int keyVersion = ((ASN1Integer) keySequence.get(0)).asBigInteger().intValue();

        if (keyVersion != 0 && keyVersion != 1) {
            throw new IOException("Incorrect type of sequence");
        }

        modulus = ((ASN1Integer) keySequence.get(1)).asBigInteger();
        exponent = ((ASN1Integer) keySequence.get(3)).asBigInteger();
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public BigInteger getExponent() {
        return exponent;
    }

    public byte[] serialize() {
        return new ASN1Sequence(
                new ASN1Integer(0),
                new ASN1Sequence(
                        new ASN1ObjectIdentifier(ALGO),
                        new ASN1Null()),
                new ASN1OctetString(
                        new ASN1Sequence(
                                new ASN1Integer(),
                                new ASN1Integer(modulus),
                                new ASN1Integer(),
                                new ASN1Integer(exponent),
                                new ASN1Integer(),
                                new ASN1Integer(),
                                new ASN1Integer(),
                                new ASN1Integer(),
                                new ASN1Integer())
                                .serialize()))
                .serialize();
    }
}
