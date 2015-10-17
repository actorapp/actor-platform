/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public class SessionHello extends ProtoStruct {
    public static final byte HEADER = (byte) 0x0F;

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
