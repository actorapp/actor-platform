package im.actor.core.entity.content.internal;

import java.io.IOException;

import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class LocalAnimation extends LocalDocument {

    private int w;
    private int h;

    public LocalAnimation(String fileName, String fileDescriptor, int fileSize, String mimeType,
                          LocalFastThumb fastThumb, int w, int h) {
        super(fileName, fileDescriptor, fileSize, mimeType, fastThumb);
        this.w = w;
        this.h = h;
    }

    public LocalAnimation(byte[] data) throws IOException {
        super(data);
    }

    public LocalAnimation(BserValues values) throws IOException {
        super(values);
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        w = values.getInt(10);
        h = values.getInt(11);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeInt(10, w);
        writer.writeInt(11, h);
    }
}
