/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.mtp.entity;

import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;

import java.io.IOException;

public class NewSessionCreated extends ProtoStruct {

    public static final byte HEADER = (byte) 0x0C;

    public long sessionId;

    public long messageId;

    public NewSessionCreated(DataInput stream) throws IOException {
        super(stream);
    }

    public NewSessionCreated(long sessionId, long messageId) {
        this.sessionId = sessionId;
        this.messageId = messageId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public long getMessageId() {
        return messageId;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(sessionId);
        bs.writeLong(messageId);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        sessionId = bs.readLong();
        messageId = bs.readLong();
    }

    @Override
    public String toString() {
        return "NewSession {" + sessionId + "}";
    }
}
