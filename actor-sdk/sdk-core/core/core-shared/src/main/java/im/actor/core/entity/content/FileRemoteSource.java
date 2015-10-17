/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.entity.FileReference;

public class FileRemoteSource extends FileSource {

    private FileReference fileReference;

    public FileRemoteSource(FileReference fileReference) {
        this.fileReference = fileReference;
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
}
