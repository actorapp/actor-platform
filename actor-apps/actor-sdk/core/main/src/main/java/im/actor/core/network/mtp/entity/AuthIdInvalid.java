/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public class AuthIdInvalid extends ProtoStruct {

    public static final byte HEADER = (byte) 0x11;

    public AuthIdInvalid(DataInput stream) throws IOException {
        super(stream);
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {

    }

    @Override
    protected void readBody(DataInput bs) throws IOException {

    }
}
