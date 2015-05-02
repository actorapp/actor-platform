/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class FileReference extends BserObject {

    public static FileReference fromBytes(byte[] data) throws IOException {
        return Bser.parse(new FileReference(), data);
    }

    private long fileId;
    private long accessHash;
    private int fileSize;
    private String fileName;

    public FileReference(long fileId, long accessHash, int fileSize, String fileName) {
        this.fileId = fileId;
        this.accessHash = accessHash;
        this.fileSize = fileSize;
        this.fileName = fileName;
    }

    private FileReference() {

    }

    public int getFileSize() {
        return fileSize;
    }

    public long getFileId() {
        return fileId;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileReference that = (FileReference) o;

        if (fileId != that.fileId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (fileId ^ (fileId >>> 32));
    }

    @Override
    public void parse(BserValues values) throws IOException {
        fileId = values.getLong(1);
        accessHash = values.getLong(2);
        fileSize = values.getInt(3);
        fileName = values.getString(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, fileId);
        writer.writeLong(2, accessHash);
        writer.writeInt(3, fileSize);
        writer.writeString(4, fileName);
    }
}
