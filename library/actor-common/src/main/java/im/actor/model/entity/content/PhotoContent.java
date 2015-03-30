package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class PhotoContent extends DocumentContent {

    public static PhotoContent photoFromBytes(byte[] data) throws IOException {
        return Bser.parse(new PhotoContent(), data);
    }

    private int w;
    private int h;

    public PhotoContent(FileSource location, String mimetype, String name, FastThumb fastThumb, int w, int h) {
        super(location, mimetype, name, fastThumb);
        this.w = w;
        this.h = h;
    }

    protected PhotoContent() {

    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.DOCUMENT_PHOTO;
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
