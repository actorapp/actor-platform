/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto.asn1;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public class ASN1OctetString extends ASN1Primitive {

    public static ASN1OctetString readOctetString(DataInput dataInput) throws IOException {
        return new ASN1OctetString(dataInput.readBytes(dataInput.getRemaining()));
    }

    private byte[] data;

    public ASN1OctetString(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public void serialize(DataOutput dataOutput) {
        dataOutput.writeByte(TAG_OCTET_STRING);
        dataOutput.writeASN1Length(data.length);
        dataOutput.writeBytes(data, 0, data.length);
    }
}
