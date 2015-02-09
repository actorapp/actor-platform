package im.actor.model.network.mtp.entity;

import im.actor.model.util.DataInput;
import im.actor.model.util.DataOutput;

import java.io.IOException;
import java.util.Arrays;

public class MessageAck extends ProtoStruct {

    public static final byte HEADER = (byte) 0x06;

    public long[] messagesIds;

    public MessageAck(DataInput stream) throws IOException {
        super(stream);
    }

    public MessageAck(Long[] _messagesIds) {
        this.messagesIds = new long[_messagesIds.length];
        for (int i = 0; i < _messagesIds.length; ++i) {
            this.messagesIds[i] = _messagesIds[i];
        }
    }

    public MessageAck(long[] messagesIds) {
        this.messagesIds = messagesIds;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeProtoLongs(messagesIds);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        messagesIds = bs.readProtoLongs();
    }

    @Override
    public String toString() {
        return "Ack " + Arrays.toString(messagesIds) + "";
    }
}
