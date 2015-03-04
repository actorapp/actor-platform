package im.actor.model.entity.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserParser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.util.DataInput;

/**
 * Created by ex3ndr on 14.02.15.
 */
public abstract class FileSource extends BserObject {

    public static FileSource fromBytes(byte[] data) throws IOException {
        BserValues reader = new BserValues(BserParser.deserialize(new DataInput(data, 0, data.length)));
        int type = reader.getInt(1);
        switch (type) {
            case 1:
                return FileLocalSource.fromValues(reader);
            case 2:
                return FileRemoteSource.fromValues(reader);
            default:
                throw new IOException("Invalid source type");
        }
    }

    public abstract int getSize();

    public abstract String getFileName();

    @Override
    public void parse(BserValues values) throws IOException {

    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (this instanceof FileLocalSource) {
            writer.writeInt(1, 1);
        } else if (this instanceof FileRemoteSource) {
            writer.writeInt(1, 2);
        } else {
            throw new IOException("Invalid source type");
        }
    }
}
