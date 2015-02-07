package im.actor.model.network.mtp.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

/**
 * Created by ex3ndr on 03.09.14.
 */
public class UnsentResponse extends ProtoStruct {

    public static final byte HEADER = (byte) 0x08;

    private long messageId;
    private long responseMessageId;
    private int len;

    public UnsentResponse(long messageId, long responseMessageId, int len) {
        this.messageId = messageId;
        this.responseMessageId = responseMessageId;
        this.len = len;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getResponseMessageId() {
        return responseMessageId;
    }

    public int getLen() {
        return len;
    }

    public UnsentResponse(InputStream stream) throws IOException {
        super(stream);
    }

    @Override
    public int getLength() {
        return 1 + 8 + 8 + 4;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(OutputStream bs) throws IOException {
        writeLong(messageId, bs);
        writeLong(responseMessageId, bs);
        writeInt(len, bs);
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        messageId = readLong(bs);
        responseMessageId = readLong(bs);
        len = readInt(bs);
    }

    @Override
    public String toString() {
        return "UnsentResponse[" + messageId + "->" + responseMessageId + "]";
    }
}
