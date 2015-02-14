package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class FileLocalSource extends FileSource {

    protected static FileLocalSource fromValues(BserValues reader) throws IOException {
        FileLocalSource fileLocalSource = new FileLocalSource();
        fileLocalSource.parse(reader);
        return fileLocalSource;
    }

    private String fileName;

    public FileLocalSource(String fileName) {
        this.fileName = fileName;
    }

    private FileLocalSource() {

    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        fileName = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeString(2, fileName);
    }
}
