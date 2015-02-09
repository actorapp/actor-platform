package im.actor.model.network.mtp.entity;

import im.actor.model.util.DataInput;
import im.actor.model.util.DataOutput;

import java.io.IOException;

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

    public UnsentMessage(DataInput stream) throws IOException {
        super(stream);
    }

    public long getMessageId() {
        return messageId;
    }

    public int getLen() {
        return len;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(messageId);
        bs.writeInt(len);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        messageId = bs.readLong();
        len = bs.readInt();
    }

    @Override
    public String toString() {
        return "UnsentMessage[" + messageId + "]";
    }
}
