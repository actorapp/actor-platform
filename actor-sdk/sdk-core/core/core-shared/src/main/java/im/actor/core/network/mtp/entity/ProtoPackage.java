/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public class ProtoPackage extends ProtoObject {

    private long authId;
    private long sessionId;
    private ProtoMessage payload;

    public ProtoPackage(DataInput stream) throws IOException {
        super(stream);
    }

    public ProtoPackage(long authId, long sessionId, ProtoMessage payload) {
        this.authId = authId;
        this.sessionId = sessionId;
        this.payload = payload;
    }

    public long getAuthId() {
        return authId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public ProtoMessage getPayload() {
        return payload;
    }

    @Override
    public void writeObject(DataOutput bs) throws IOException {
        bs.writeLong(authId);
        bs.writeLong(sessionId);
        payload.writeObject(bs);
    }

    @Override
    public ProtoObject readObject(DataInput bs) throws IOException {
        authId = bs.readLong();
        sessionId = bs.readLong();
        payload = new ProtoMessage(bs);
        return this;
    }

    @Override
    public String toString() {
        return "ProtoPackage[" + authId + "|" + sessionId + "]";
    }
}
