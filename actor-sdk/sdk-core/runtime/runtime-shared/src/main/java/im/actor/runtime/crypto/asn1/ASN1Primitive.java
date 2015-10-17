/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto.asn1;

import im.actor.runtime.bser.DataOutput;

public abstract class ASN1Primitive {

    public abstract void serialize(DataOutput dataOutput);

    public byte[] serialize() {
        DataOutput res = new DataOutput();
        serialize(res);
        return res.toByteArray();
    }

    public static final int TAG_BOOLEAN = 0x01;
    public static final int TAG_INTEGER = 0x02;
    public static final int TAG_BIT_STRING = 0x03;
    public static final int TAG_OCTET_STRING = 0x04;
    public static final int TAG_NULL = 0x05;
    public static final int TAG_OBJECT_IDENTIFIER = 0x06;
    public static final int TAG_EXTERNAL = 0x08;
    public static final int TAG_ENUMERATED = 0x0a;
    public static final int TAG_SEQUENCE = 0x10;
    public static final int TAG_SEQUENCE_OF = 0x10; // for completeness - used to model a SEQUENCE of the same type.
    public static final int TAG_SET = 0x11;
    public static final int TAG_SET_OF = 0x11; // for completeness - used to model a SET of the same type.

    public static final int TAG_NUMERIC_STRING = 0x12;
    public static final int TAG_PRINTABLE_STRING = 0x13;
    public static final int TAG_T61_STRING = 0x14;
    public static final int TAG_VIDEOTEX_STRING = 0x15;
    public static final int TAG_IA5_STRING = 0x16;
    public static final int TAG_UTC_TIME = 0x17;
    public static final int TAG_GENERALIZED_TIME = 0x18;
    public static final int TAG_GRAPHIC_STRING = 0x19;
    public static final int TAG_VISIBLE_STRING = 0x1a;
    public static final int TAG_GENERAL_STRING = 0x1b;
    public static final int TAG_UNIVERSAL_STRING = 0x1c;
    public static final int TAG_BMP_STRING = 0x1e;
    public static final int TAG_UTF8_STRING = 0x0c;

    public static final int TAG_CONSTRUCTED = 0x20;
    public static final int TAG_APPLICATION = 0x40;
    public static final int TAG_TAGGED = 0x80;
}