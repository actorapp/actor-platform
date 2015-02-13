package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.messages.FastThumb;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class VideoMessage extends AbsFileMessage {

    private int duration;
    private int w;
    private int h;
    private FastThumb fastThumb;

    public VideoMessage(FileLocation location, boolean isDownloaded, int duration, int w, int h,
                        FastThumb fastThumb, boolean isEncrypted) {
        super(location, isDownloaded, isEncrypted);
        this.duration = duration;
        this.w = w;
        this.h = h;
        this.fastThumb = fastThumb;
    }

    public VideoMessage(String uploadPath, int duration, int w, int h, FastThumb fastThumb,
                        boolean isEncrypted) {
        super(uploadPath, isEncrypted);
        this.duration = duration;
        this.w = w;
        this.h = h;
        this.fastThumb = fastThumb;
    }

    public VideoMessage() {
    }

    @Override
    public AbsFileMessage downloaded() {
        return new VideoMessage(location, true, duration, w, h, fastThumb, isEncrypted);
    }

    @Override
    public AbsFileMessage undownloaded() {
        return new VideoMessage(location, false, duration, w, h, fastThumb, isEncrypted);
    }

    public VideoMessage uploaded(FileLocation fileLocation) {
        return new VideoMessage(fileLocation, true, duration, w, h, fastThumb, isEncrypted);
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

    public FastThumb getFastThumb() {
        return fastThumb;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        duration = values.getInt(4);
        w = values.getInt(5);
        h = values.getInt(6);
        fastThumb = values.optObj(7, FastThumb.class);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeInt(4, duration);
        writer.writeInt(5, w);
        writer.writeInt(6, h);
        if (fastThumb != null) {
            writer.writeObject(7, fastThumb);
        }
    }
}
