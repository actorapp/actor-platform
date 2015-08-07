/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.mtp.entity;

import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;

import java.io.IOException;

public class Pong extends ProtoStruct {

    public static final byte HEADER = (byte) 0x02;

    private long randomId;

    public Pong(DataInput stream) throws IOException {
        super(stream);
    }

    public Pong(long randomId) {
        this.randomId = randomId;
    }

    public long getRandomId() {
        return randomId;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(randomId);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        randomId = bs.readLong();
    }

    @Override
    public String toString() {
        return "Pong{" + randomId + "}";
    }
}
