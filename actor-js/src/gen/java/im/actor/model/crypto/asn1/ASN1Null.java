package im.actor.model.crypto.asn1;

import im.actor.model.droidkit.bser.DataOutput;

/**
 * Created by ex3ndr on 09.03.15.
 */
public class ASN1Null extends ASN1Primitive {
    @Override
    public void serialize(DataOutput dataOutput) {
        dataOutput.writeByte(TAG_NULL);
        dataOutput.writeByte(0);
    }
}
