/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.mtp.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;

public class SessionLost extends ProtoStruct {
    public static final int HEADER = 0x10;

    public SessionLost(DataInput stream) throws IOException {
        super(stream);
    }

    public SessionLost() {
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
