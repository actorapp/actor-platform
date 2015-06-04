/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class ObsoleteLocalFileSource extends ObsoleteFileSource {

    private String fileName;
    private String fileDescriptor;
    private int size;

    public ObsoleteLocalFileSource(BserValues values) throws IOException {
        parse(values);
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileDescriptor() {
        return fileDescriptor;
    }

    public int getSize() {
        return size;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        fileName = values.getString(2);
        size = values.getInt(3);
        fileDescriptor = values.getString(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
