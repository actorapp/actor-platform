/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content.internal;

import java.io.IOException;

import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class LocalDocument extends AbsLocalContent {
    private String fileName;
    private String fileDescriptor;
    private int fileSize;
    private String mimeType;
    private LocalFastThumb fastThumb;

    public LocalDocument(String fileName, String fileDescriptor, int fileSize, String mimeType, LocalFastThumb fastThumb) {
        this.fileName = fileName;
        this.fileDescriptor = fileDescriptor;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.fastThumb = fastThumb;
    }

    public LocalDocument(byte[] data) throws IOException {
        load(data);
    }

    public LocalDocument(BserValues values) throws IOException {
        parse(values);
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileDescriptor() {
        return fileDescriptor;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public LocalFastThumb getFastThumb() {
        return fastThumb;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        fileName = values.getString(1);
        fileDescriptor = values.getString(2);
        fileSize = values.getInt(3);
        mimeType = values.getString(4);
        byte[] rawFT = values.optBytes(5);
        if (rawFT != null) {
            fastThumb = new LocalFastThumb(rawFT);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeString(1, fileName);
        writer.writeString(2, fileDescriptor);
        writer.writeInt(3, fileSize);
        writer.writeString(4, mimeType);
        if (fastThumb != null) {
            writer.writeObject(5, fastThumb);
        }
    }
}
