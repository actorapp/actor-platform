/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto.asn1;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public class ASN1ObjectIdentifier extends ASN1Primitive {

    public static ASN1ObjectIdentifier readObjectIdentifier(DataInput dataInput) throws IOException {
        String res;

        // Reading first byte
        int firstByte = dataInput.readByte();
        long firstValue = firstByte & 0x7F;

        if (firstValue < 40) {
            res = "0";
        } else if (firstValue < 80) {
            res = "1." + (firstValue - 40);
        } else {
            res = "2." + (firstValue - 80);
        }

        // Reading other segments
        long value = 0;
        while (!dataInput.isEOF()) {
            int b = dataInput.readByte();
            value += (b & 0x7f);

            // end of number reached
            if ((b & 0x80) == 0) {
                res += '.';
                res += value;
                value = 0;
            } else {
                value <<= 7;
            }
        }

        return new ASN1ObjectIdentifier(res);
    }

    private String identifier;

    public ASN1ObjectIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void serialize(DataOutput dataOutput) {

        dataOutput.writeByte(TAG_OBJECT_IDENTIFIER);

        DataOutput content = new DataOutput();

        String[] items = identifier.split("\\.");
        int val1 = Integer.parseInt(items[0]);
        int val2 = Integer.parseInt(items[1]);

        // TODO: Add check for values
        // http://luca.ntop.org/Teaching/Appunti/asn1.html

        content.writeByte(val1 * 40 + val2);

        for (int i = 2; i < items.length; i++) {
            long value = Long.parseLong(items[i]);

            byte[] result = new byte[9];
            int pos = 8;
            result[pos] = (byte) ((int) value & 0x7f);
            while (value >= (1L << 7)) {
                value >>= 7;
                result[--pos] = (byte) ((int) value & 0x7f | 0x80);
            }
            content.writeBytes(result, pos, 9 - pos);
        }

        byte[] contentV = content.toByteArray();
        dataOutput.writeASN1Length(contentV.length);
        dataOutput.writeBytes(contentV, 0, contentV.length);
    }
}
