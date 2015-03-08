package im.actor.model.droidkit.bser;

import java.io.IOException;

/**
 * Created by ex3ndr on 17.10.14.
 */
public abstract class BserObject {
    public BserObject() {

    }

    public byte[] toByteArray() {
        DataOutput outputStream = new DataOutput();
        BserWriter writer = new BserWriter(outputStream);
        try {
            serialize(writer);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IO exception");
        }
        return outputStream.toByteArray();
    }

    public abstract void parse(BserValues values) throws IOException;

    public abstract void serialize(BserWriter writer) throws IOException;
}
