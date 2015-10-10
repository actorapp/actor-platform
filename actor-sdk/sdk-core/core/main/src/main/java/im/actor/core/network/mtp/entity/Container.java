/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public class Container extends ProtoStruct {

    public static final byte HEADER = (byte) 0x0A;

    private ProtoMessage[] messages;

    public Container(DataInput stream) throws IOException {
        super(stream);
    }

    public Container(ProtoMessage[] messages) {
        this.messages = messages;
    }

    public ProtoMessage[] getMessages() {
        return messages;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        if (messages != null && messages.length > 0) {
            bs.writeVarInt(messages.length);
            for (ProtoMessage m : messages) {
                m.writeObject(bs);
            }
        } else {
            bs.writeVarInt(0);
        }
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        int size = (int) bs.readVarInt();
        messages = new ProtoMessage[size];
        for (int i = 0; i < size; ++i) {
            messages[i] = new ProtoMessage(bs);
        }
    }

    @Override
    public String toString() {
        return "Conatiner[" + messages.length + " items]";
    }
}
