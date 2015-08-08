/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public abstract class ProtoStruct extends ProtoObject {

    protected ProtoStruct(DataInput stream) throws IOException {
        super(stream);
    }

    protected ProtoStruct() {
    }

    protected abstract byte getHeader();

    @Override
    public final void writeObject(DataOutput bs) throws IOException {
        byte header = getHeader();
        if (header != 0) {
            bs.writeByte(header);
        }
        writeBody(bs);
    }

    @Override
    public final ProtoObject readObject(DataInput bs) throws IOException {
        readBody(bs);
        return this;
    }

    protected abstract void writeBody(DataOutput bs) throws IOException;

    protected abstract void readBody(DataInput bs) throws IOException;
}
