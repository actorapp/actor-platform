/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class FileLocalSource extends FileSource {

    protected static FileLocalSource fromValues(BserValues reader) throws IOException {
        FileLocalSource fileLocalSource = new FileLocalSource();
        fileLocalSource.parse(reader);
        return fileLocalSource;
    }

    private String fileName;
    private String fileDescriptor;
    private int size;

    public FileLocalSource(String fileName, int size, String fileDescriptor) {
        this.fileName = fileName;
        this.size = size;
        this.fileDescriptor = fileDescriptor;
    }

    private FileLocalSource() {

    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public int getSize() {
        return size;
    }

    public String getFileDescriptor() {
        return fileDescriptor;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        fileName = values.getString(2);
        size = values.getInt(3);
        fileDescriptor = values.getString(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeString(2, fileName);
        writer.writeInt(3, size);
        writer.writeString(4, fileDescriptor);
    }
}
