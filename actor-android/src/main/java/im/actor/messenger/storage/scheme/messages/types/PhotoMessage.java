package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.messages.FastThumb;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class PhotoMessage extends AbsFileMessage {
    private int w;
    private int h;
    private FastThumb fastThumb;

    public PhotoMessage(FileLocation location, boolean isDownloaded, int w, int h, FastThumb fastThumb,
                        boolean isEncrypted) {
        super(location, isDownloaded, isEncrypted);
        this.w = w;
        this.h = h;
        this.fastThumb = fastThumb;
    }

    public PhotoMessage(FileLocation location, int w, int h, FastThumb fastThumb, boolean isEncrypted) {
        super(location, false, isEncrypted);
        this.w = w;
        this.h = h;
        this.fastThumb = fastThumb;
    }

    public PhotoMessage(String uploadPath, int w, int h, FastThumb fastThumb, boolean isEncrypted) {
        super(uploadPath, isEncrypted);
        this.w = w;
        this.h = h;
        this.fastThumb = fastThumb;
    }

    public PhotoMessage() {
    }

    @Override
    public AbsFileMessage downloaded() {
        return new PhotoMessage(location, true, w, h, fastThumb, isEncrypted);
    }

    @Override
    public AbsFileMessage undownloaded() {
        return new PhotoMessage(location, false, w, h, fastThumb, isEncrypted);
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

    public PhotoMessage uploaded(FileLocation fileLocation) {
        return new PhotoMessage(fileLocation, true, w, h, fastThumb, isEncrypted);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        w = values.getInt(4);
        h = values.getInt(5);
        fastThumb = values.getObj(6, FastThumb.class);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeInt(4, w);
        writer.writeInt(5, h);
        if (fastThumb != null) {
            writer.writeObject(6, fastThumb);
        }
    }
}
