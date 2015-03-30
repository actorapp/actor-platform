package im.actor.model.entity.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.entity.FileReference;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class FileRemoteSource extends FileSource {

    protected static FileRemoteSource fromValues(BserValues reader) throws IOException {
        FileRemoteSource fileLocalSource = new FileRemoteSource();
        fileLocalSource.parse(reader);
        return fileLocalSource;
    }

    private FileReference fileReference;

    public FileRemoteSource(FileReference fileReference) {
        this.fileReference = fileReference;
    }

    private FileRemoteSource() {

    }

    public FileReference getFileReference() {
        return fileReference;
    }

    @Override
    public int getSize() {
        return fileReference.getFileSize();
    }

    @Override
    public String getFileName() {
        return fileReference.getFileName();
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        fileReference = FileReference.fromBytes(values.getBytes(2));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeObject(2, fileReference);
    }
}
