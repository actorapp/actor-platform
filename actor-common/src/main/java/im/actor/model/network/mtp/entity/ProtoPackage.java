package im.actor.model.network.mtp.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

public class ProtoPackage extends ProtoObject {

    private long authId;
    private long sessionId;
    private ProtoMessage payload;

    public ProtoPackage(InputStream stream) throws IOException {
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
    public void writeObject(OutputStream bs) throws IOException {
        writeLong(authId, bs);
        writeLong(sessionId, bs);
        payload.writeObject(bs);
    }

    @Override
    public ProtoObject readObject(InputStream bs) throws IOException {
        authId = readLong(bs);
        sessionId = readLong(bs);
        payload = new ProtoMessage(bs);
        return this;
    }

    @Override
    public int getLength() {
        return 8 + 8 + payload.getLength();
    }

    @Override
    public String toString() {
        return "ProtoPackage[" + authId + "|" + sessionId + "]";
    }
}
