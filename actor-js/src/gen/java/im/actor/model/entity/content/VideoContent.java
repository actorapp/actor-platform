package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class VideoContent extends DocumentContent {

    public static VideoContent videoFromBytes(byte[] data) throws IOException {
        return Bser.parse(new VideoContent(), data);
    }

    private int duration;
    private int w;
    private int h;

    public VideoContent(FileSource location, String mimetype, String name, FastThumb fastThumb, int duration, int w, int h) {
        super(location, mimetype, name, fastThumb);
        this.duration = duration;
        this.w = w;
        this.h = h;
    }

    private VideoContent() {

    }

    public int getDuration() {
        return duration;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.DOCUMENT_VIDEO;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        duration = values.getInt(10);
        w = values.getInt(11);
        h = values.getInt(12);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeInt(10, duration);
        writer.writeInt(11, w);
        writer.writeInt(12, h);
    }
}
