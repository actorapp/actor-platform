package im.actor.model.network.mtp.entity;

import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;

import java.io.IOException;

/**
 * Created by ex3ndr on 03.09.14.
 */
public class RequestResend extends ProtoStruct {

    public static final byte HEADER = (byte) 0x09;

    private long messageId;

    public RequestResend(long messageId) {
        this.messageId = messageId;
    }

    public RequestResend(DataInput stream) throws IOException {
        super(stream);
    }

    public long getMessageId() {
        return messageId;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeLong(messageId);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        messageId = bs.readLong();
    }
}
