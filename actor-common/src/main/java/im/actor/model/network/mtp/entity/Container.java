package im.actor.model.network.mtp.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

public class Container extends ProtoStruct {

    public static final byte HEADER = (byte) 0x0A;

    private ProtoMessage[] messages;

    public Container(InputStream stream) throws IOException {
        super(stream);
    }

    public Container(ProtoMessage[] messages) {
        this.messages = messages;
    }

    public ProtoMessage[] getMessages() {
        return messages;
    }

    @Override
    public int getLength() {
        int messagesLength = 0;
        if (messages.length > 0) {
            for (ProtoMessage m : messages) {
                messagesLength += m.getLength();
            }
        }
        return 1 + varintSize(messages.length) + messagesLength;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(OutputStream bs) throws IOException {
        if (messages != null && messages.length > 0) {
            writeVarInt(messages.length, bs);
            for (ProtoMessage m : messages) {
                m.writeObject(bs);
            }
        } else {
            writeVarInt(0, bs);
        }
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        int size = (int) readVarInt(bs);
        messages = new ProtoMessage[size];
        for (int i = 0; i < size; ++i) {
            messages[i] = new ProtoMessage(bs);
        }
    }

    @Override
    public String toString() {
        return "Conatiner[" + messages.length + " items]";
    }
}
