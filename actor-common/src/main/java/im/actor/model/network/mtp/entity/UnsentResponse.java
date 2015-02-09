package im.actor.model.network.mtp.entity;

import im.actor.model.util.DataInput;
import im.actor.model.util.DataOutput;

import java.io.IOException;

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

    public UnsentResponse(DataInput stream) throws IOException {
        super(stream);
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(messageId);
        bs.writeLong(responseMessageId);
        bs.writeInt(len);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        messageId = bs.readLong();
        responseMessageId = bs.readLong();
        len = bs.readInt();
    }

    @Override
    public String toString() {
        return "UnsentResponse[" + messageId + "->" + responseMessageId + "]";
    }
}
