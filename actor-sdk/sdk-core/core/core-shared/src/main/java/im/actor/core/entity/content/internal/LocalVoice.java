/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content.internal;

import java.io.IOException;

import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class LocalVoice extends LocalDocument {

    private int duration;

    public LocalVoice(String fileName,
                      String fileDescriptor,
                      int fileSize,
                      String mimeType,
                      int duration) {
        super(fileName, fileDescriptor, fileSize, mimeType, null);
        this.duration = duration;
    }

    public LocalVoice(byte[] data) throws IOException {
        super(data);
    }

    public LocalVoice(BserValues values) throws IOException {
        super(values);
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        duration = values.getInt(10);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeInt(10, duration);
    }
}
