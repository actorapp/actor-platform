package im.actor.model.network.mtp.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

public class MTRpcResponse extends ProtoStruct {

    public static final byte HEADER = (byte) 0x04;

    private long messageId;
    private byte[] payload;

    public MTRpcResponse(InputStream stream) throws IOException {
        super(stream);
    }

    public long getMessageId() {
        return messageId;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public int getLength() {
        return 1 + 8 + varintSize(payload.length) + payload.length;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(OutputStream bs) throws IOException {
        writeLong(messageId, bs);
        writeProtoBytes(payload, bs);
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        messageId = readLong(bs);
        payload = readProtoBytes(bs);
    }

    @Override
    public String toString() {
        return "ResponseBox [" + messageId + "]";
    }
}
