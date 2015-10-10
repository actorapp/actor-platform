/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto.asn1;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public class ASN1BitString extends ASN1Primitive {

    public static ASN1BitString readBitString(DataInput dataInput) throws IOException {
        int paddingBytes = dataInput.readByte();
        return new ASN1BitString(paddingBytes, dataInput.readBytes(dataInput.getRemaining()));
    }

    private int paddingBit;
    private byte[] content;

    public ASN1BitString(int paddingBit, byte[] content) {
        this.paddingBit = paddingBit;
        this.content = content;
    }

    public int getPaddingBit() {
        return paddingBit;
    }

    public byte[] getContent() {
        return content;
    }

    @Override
    public void serialize(DataOutput dataOutput) {
        dataOutput.writeByte(TAG_BIT_STRING);
        dataOutput.writeASN1Length(content.length + 1);
        dataOutput.writeByte(paddingBit);
        dataOutput.writeBytes(content, 0, content.length);
    }
}
