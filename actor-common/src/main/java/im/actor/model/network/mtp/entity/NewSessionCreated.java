package im.actor.model.network.mtp.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

public class NewSessionCreated extends ProtoStruct {

    public static final byte HEADER = (byte) 0x0C;

    public long sessionId;

    public long messageId;

    public NewSessionCreated(InputStream stream) throws IOException {
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
    public int getLength() {
        return 1 + 8 + 8;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(OutputStream bs) throws IOException {
        writeLong(sessionId, bs);
        writeLong(messageId, bs);
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        sessionId = readLong(bs);
        messageId = readLong(bs);
    }

    @Override
    public String toString() {
        return "NewSession {" + sessionId + "}";
    }
}
