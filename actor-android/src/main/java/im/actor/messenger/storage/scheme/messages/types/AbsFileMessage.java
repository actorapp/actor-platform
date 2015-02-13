package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

import im.actor.messenger.storage.scheme.FileLocation;

/**
 * Created by ex3ndr on 25.10.14.
 */
public abstract class AbsFileMessage extends AbsMessage {
    protected FileLocation location;
    protected String uploadPath;
    protected boolean isDownloaded;

    public AbsFileMessage(FileLocation location, boolean isDownloaded, boolean isEncrypted) {
        super(isEncrypted);
        this.location = location;
        this.isDownloaded = isDownloaded;
    }

    public AbsFileMessage(String uploadPath, boolean isEncrypted) {
        super(isEncrypted);
        this.uploadPath = uploadPath;
    }

    public AbsFileMessage() {

    }

    public abstract AbsFileMessage downloaded();

    public abstract AbsFileMessage undownloaded();

    public abstract AbsFileMessage uploaded(FileLocation fileLocation);

    public FileLocation getLocation() {
        return location;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        isDownloaded = values.optBool(1);
        uploadPath = values.optString(2);
        location = values.optObj(3, FileLocation.class);
        isEncrypted = values.getBool(10, true);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeBool(1, isDownloaded);
        if (uploadPath != null) {
            writer.writeString(2, uploadPath);
        } else {
            writer.writeObject(3, location);
        }
        writer.writeBool(10, isEncrypted);
    }
}
