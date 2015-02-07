package im.actor.model.network.mtp.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ProtoObject {

    protected ProtoObject(InputStream stream) throws IOException {
        readObject(stream);
    }

    protected ProtoObject() {

    }

    public abstract void writeObject(OutputStream bs) throws IOException;

    public abstract ProtoObject readObject(InputStream bs) throws IOException;

    public byte[] toByteArray() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            writeObject(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public abstract int getLength();
}