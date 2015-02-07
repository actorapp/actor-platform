package im.actor.model.network.mtp.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import static im.actor.model.util.StreamingUtils.*;

public class MessageAck extends ProtoStruct {

    public static final byte HEADER = (byte) 0x06;

    public long[] messagesIds;

    public MessageAck(InputStream stream) throws IOException {
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
    public int getLength() {
        return 1 + varintSize(messagesIds.length) + (messagesIds.length * 8);
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(OutputStream bs) throws IOException {
        writeProtoLongs(messagesIds, bs);
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        messagesIds = readProtoLongs(bs);
    }

    @Override
    public String toString() {
        return "Ack " + Arrays.toString(messagesIds) + "";
    }
}
