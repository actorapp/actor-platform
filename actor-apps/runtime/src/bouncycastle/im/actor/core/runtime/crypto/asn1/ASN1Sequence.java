/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.runtime.crypto.asn1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.bser.DataInput;
import im.actor.core.bser.DataOutput;

public class ASN1Sequence extends im.actor.core.runtime.crypto.asn1.ASN1Primitive {

    public static ASN1Sequence readSequence(DataInput dataInput) throws IOException {
        ASN1Sequence sequence = new ASN1Sequence();
        while (!dataInput.isEOF()) {
            sequence.add(ASN1.readObject(dataInput));
        }
        return sequence;
    }

    private ArrayList<im.actor.core.runtime.crypto.asn1.ASN1Primitive> sequence;

    public ASN1Sequence() {
        this.sequence = new ArrayList<im.actor.core.runtime.crypto.asn1.ASN1Primitive>();
    }

    public ASN1Sequence(List<im.actor.core.runtime.crypto.asn1.ASN1Primitive> sequence) {
        this.sequence = new ArrayList<im.actor.core.runtime.crypto.asn1.ASN1Primitive>(sequence);
    }

    public ASN1Sequence(im.actor.core.runtime.crypto.asn1.ASN1Primitive... sequence) {
        this.sequence = new ArrayList<im.actor.core.runtime.crypto.asn1.ASN1Primitive>();
        for (im.actor.core.runtime.crypto.asn1.ASN1Primitive p : sequence) {
            this.sequence.add(p);
        }
    }

    public void add(im.actor.core.runtime.crypto.asn1.ASN1Primitive item) {
        sequence.add(item);
    }

    public ArrayList<im.actor.core.runtime.crypto.asn1.ASN1Primitive> getSequence() {
        return sequence;
    }

    public int size() {
        return sequence.size();
    }

    public im.actor.core.runtime.crypto.asn1.ASN1Primitive get(int index) {
        return sequence.get(index);
    }

    @Override
    public void serialize(DataOutput dataOutput) {
        dataOutput.writeByte(TAG_SEQUENCE | TAG_CONSTRUCTED);
        DataOutput content = new DataOutput();
        for (im.actor.core.runtime.crypto.asn1.ASN1Primitive primitive : sequence) {
            primitive.serialize(content);
        }
        byte[] contentV = content.toByteArray();
        dataOutput.writeASN1Length(contentV.length);
        dataOutput.writeBytes(contentV, 0, contentV.length);
    }
}
