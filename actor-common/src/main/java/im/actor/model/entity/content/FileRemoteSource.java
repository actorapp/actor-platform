package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.entity.FileLocation;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class FileRemoteSource extends FileSource {

    protected static FileRemoteSource fromValues(BserValues reader) throws IOException {
        FileRemoteSource fileLocalSource = new FileRemoteSource();
        fileLocalSource.parse(reader);
        return fileLocalSource;
    }

    private FileLocation fileLocation;

    public FileRemoteSource(FileLocation fileLocation) {
        this.fileLocation = fileLocation;
    }

    private FileRemoteSource() {

    }

    public FileLocation getFileLocation() {
        return fileLocation;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        fileLocation = FileLocation.fromBytes(values.getBytes(2));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeObject(2, fileLocation);
    }
}
