/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.file.entity;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.KeyValueItem;

public class Downloaded extends BserObject implements KeyValueItem {

    public static Downloaded fromBytes(byte[] data) throws IOException {
        return Bser.parse(new Downloaded(), data);
    }

    private long fileId;
    private int fileSize;
    private String descriptor;

    public Downloaded(long fileId, int fileSize, String descriptor) {
        this.fileId = fileId;
        this.fileSize = fileSize;
        this.descriptor = descriptor;
    }

    private Downloaded() {

    }

    public long getFileId() {
        return fileId;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        fileId = values.getLong(1);
        fileSize = values.getInt(2);
        descriptor = values.getString(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, fileId);
        writer.writeInt(2, fileSize);
        writer.writeString(3, descriptor);
    }

    @Override
    public long getEngineId() {
        return fileId;
    }
}
