package im.actor.model.network.mtp.entity;

import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;

import java.io.IOException;

public abstract class ProtoObject {

    protected ProtoObject(DataInput stream) throws IOException {
        readObject(stream);
    }

    protected ProtoObject() {

    }

    public abstract void writeObject(DataOutput bs) throws IOException;

    public abstract ProtoObject readObject(DataInput bs) throws IOException;

    public byte[] toByteArray() {
        DataOutput outputStream = new DataOutput();
        try {
            writeObject(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
}