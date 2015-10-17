/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

public class FileLocalSource extends FileSource {

    private String fileName;
    private String fileDescriptor;
    private int size;

    public FileLocalSource(String fileName, int size, String fileDescriptor) {
        this.fileName = fileName;
        this.size = size;
        this.fileDescriptor = fileDescriptor;
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
}
