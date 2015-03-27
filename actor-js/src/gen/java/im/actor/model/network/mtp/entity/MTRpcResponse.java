package im.actor.model.network.mtp.entity;

import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;

import java.io.IOException;

public class MTRpcResponse extends ProtoStruct {

    public static final byte HEADER = (byte) 0x04;

    private long messageId;
    private byte[] payload;

    public MTRpcResponse(DataInput stream) throws IOException {
        super(stream);
    }

    public long getMessageId() {
        return messageId;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(messageId);
        bs.writeProtoBytes(payload, 0, payload.length);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        messageId = bs.readLong();
        payload = bs.readProtoBytes();
    }

    @Override
    public String toString() {
        return "ResponseBox [" + messageId + "]";
    }
}
