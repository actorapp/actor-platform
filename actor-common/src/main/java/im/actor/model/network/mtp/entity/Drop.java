package im.actor.model.network.mtp.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

public class Drop extends ProtoStruct {

    public static final byte HEADER = (byte) 0x0D;

    public long messageId;

    public String message;

    public Drop(InputStream stream) throws IOException {
        super(stream);
    }

    public Drop(long messageId, String message) {
        this.messageId = messageId;
        this.message = message;
    }

    public long getMessageId() {
        return messageId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int getLength() {
        return 1 + 8 + stringSize(message);
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(OutputStream bs) throws IOException {
        writeLong(messageId, bs);
        writeProtoString(message, bs);
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        messageId = readLong(bs);
        message = readProtoString(bs);
    }

    @Override
    public String toString() {
        return "Drop[" + message + "]";
    }
}
