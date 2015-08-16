/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto.asn1;

import java.io.IOException;
import java.math.BigInteger;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public class ASN1Integer extends ASN1Primitive {

    public static ASN1Integer readInteger(DataInput dataInput) throws IOException {
        return new ASN1Integer(dataInput.readBytes(dataInput.getRemaining()));
    }

    private byte[] data;

    public ASN1Integer(byte[] data) {
        this.data = data;
    }

    public ASN1Integer(BigInteger data) {
        this.data = data.toByteArray();
    }

    public ASN1Integer() {
        this.data = BigInteger.ZERO.toByteArray();
    }

    public ASN1Integer(int val) {
        this.data = new BigInteger("" + val).toByteArray();
    }

    public byte[] getData() {
        return data;
    }

    public BigInteger asBigInteger() {
        // ASN1 is fully compatible with Big Integer
        return new BigInteger(data);
    }

    @Override
    public void serialize(DataOutput dataOutput) {
        dataOutput.writeByte(TAG_INTEGER);
        dataOutput.writeASN1Length(data.length);
        dataOutput.writeBytes(data, 0, data.length);
    }
}
