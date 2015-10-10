/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto.asn1;

import im.actor.runtime.bser.DataOutput;

public class ASN1Null extends ASN1Primitive {
    @Override
    public void serialize(DataOutput dataOutput) {
        dataOutput.writeByte(TAG_NULL);
        dataOutput.writeByte(0);
    }
}
