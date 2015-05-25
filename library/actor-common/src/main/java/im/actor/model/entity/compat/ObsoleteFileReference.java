/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class ObsoleteFileReference extends BserObject {

    private long fileId;
    private long accessHash;
    private int fileSize;
    private String fileName;

    public ObsoleteFileReference(byte[] data) throws IOException {
        load(data);
    }

    public ObsoleteFileReference(BserValues values) throws IOException {
        parse(values);
    }

    public long getFileId() {
        return fileId;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
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
        throw new UnsupportedOperationException();
    }
}