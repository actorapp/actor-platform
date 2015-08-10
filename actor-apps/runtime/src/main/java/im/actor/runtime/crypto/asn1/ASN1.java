/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto.asn1;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;

public class ASN1 {
    public static ASN1Primitive readObject(byte[] sourceData) throws IOException {
        return readObject(sourceData, 0, sourceData.length);
    }

    public static ASN1Primitive readObject(byte[] sourceData, int offset, int len) throws IOException {
        return readObject(new DataInput(sourceData, offset, len));
    }

    public static ASN1Primitive readObject(DataInput dataInput) throws IOException {
        int tag = dataInput.readASN1Tag();
        int tagNumber = dataInput.readASN1TagNumber(tag);
        boolean isConstructed = (tag & ASN1Primitive.TAG_CONSTRUCTED) != 0;
        int length = dataInput.readASN1Length();

        // TODO: Add length check

        if ((tag & ASN1Primitive.TAG_APPLICATION) != 0) {
            // return new BERApplicationSpecificParser(tagNo, sp).getLoadedObject();
            throw new IOException();
        }

        if ((tag & ASN1Primitive.TAG_TAGGED) != 0) {
            // return new BERTaggedObjectParser(true, tagNo, sp).getLoadedObject();
            throw new IOException();
        }

        DataInput objDataInput;
        if (length > 0) {
            objDataInput = new DataInput(dataInput.getData(), dataInput.getOffset(), length);
            dataInput.skip(length);
        } else {
            objDataInput = dataInput;
        }

        switch (tagNumber) {
            case ASN1Primitive.TAG_SEQUENCE:
                return ASN1Sequence.readSequence(objDataInput);
            case ASN1Primitive.TAG_INTEGER:
                return ASN1Integer.readInteger(objDataInput);
            case ASN1Primitive.TAG_OBJECT_IDENTIFIER:
                return ASN1ObjectIdentifier.readObjectIdentifier(objDataInput);
            case ASN1Primitive.TAG_NULL:
                return new ASN1Null();
            case ASN1Primitive.TAG_BIT_STRING:
                return ASN1BitString.readBitString(objDataInput);
            case ASN1Primitive.TAG_OCTET_STRING:
                return ASN1OctetString.readOctetString(objDataInput);
            default:
                throw new IOException("Unsupported tag number #" + tagNumber);
        }
    }
}
