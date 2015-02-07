package im.actor.model.network.mtp.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

public abstract class ProtoStruct extends ProtoObject {

    protected ProtoStruct(InputStream stream) throws IOException {
        super(stream);
    }

    protected ProtoStruct() {
    }

    protected abstract byte getHeader();

    @Override
    public final void writeObject(OutputStream bs) throws IOException {
        byte header = getHeader();
        if (header != 0) {
            writeByte(header, bs);
        }
        writeBody(bs);
    }

    @Override
    public final ProtoObject readObject(InputStream bs) throws IOException {
        readBody(bs);
        return this;
    }

    protected abstract void writeBody(OutputStream bs) throws IOException;

    protected abstract void readBody(InputStream bs) throws IOException;
}
