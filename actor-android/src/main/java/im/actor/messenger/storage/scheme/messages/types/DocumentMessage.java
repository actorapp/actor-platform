package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.messages.FastThumb;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class DocumentMessage extends AbsFileMessage {
    private String name;
    private int size;
    private FastThumb fastThumb;

    public DocumentMessage(FileLocation location, boolean isDownloaded, String name, int size,
                           boolean isEncrypted, FastThumb fastThumb) {
        super(location, isDownloaded, isEncrypted);
        this.name = name;
        this.size = size;
        this.fastThumb = fastThumb;
    }

    public DocumentMessage(String uploadPath, String name, int size, boolean isEncrypted, FastThumb fastThumb) {
        super(uploadPath, isEncrypted);
        this.name = name;
        this.size = size;
        this.fastThumb = fastThumb;
    }

    public DocumentMessage() {
    }

    @Override
    public AbsFileMessage downloaded() {
        return new DocumentMessage(location, true, name, size,
                isEncrypted, fastThumb);
    }

    @Override
    public AbsFileMessage undownloaded() {
        return new DocumentMessage(location, false, name, size, isEncrypted, fastThumb);
    }

    public DocumentMessage uploaded(FileLocation fileLocation) {
        return new DocumentMessage(fileLocation, true, name, size, isEncrypted, fastThumb);
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public FastThumb getFastThumb() {
        return fastThumb;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        name = values.getString(4);
        size = values.optInt(5);
        fastThumb = values.optObj(6, FastThumb.class);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeString(4, name);
        writer.writeInt(5, size);
        if (fastThumb != null) {
            writer.writeObject(6, fastThumb);
        }
    }
}
