package im.actor.model.network.mtp.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

/**
 * Created by ex3ndr on 03.09.14.
 */
public class UnsentMessage extends ProtoStruct {

    public static final byte HEADER = (byte) 0x07;

    private long messageId;
    private int len;

    public UnsentMessage(long messageId, int len) {
        this.messageId = messageId;
        this.len = len;
    }

    public UnsentMessage(InputStream stream) throws IOException {
        super(stream);
    }

    public long getMessageId() {
        return messageId;
    }

    public int getLen() {
        return len;
    }

    @Override
    public int getLength() {
        return 1 + 8 + 4;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(OutputStream bs) throws IOException {
        writeLong(messageId, bs);
        writeInt(len, bs);
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        messageId = readLong(bs);
        len = readInt(bs);
    }

    @Override
    public String toString() {
        return "UnsentMessage[" + messageId + "]";
    }
}
