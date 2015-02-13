package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

import im.actor.messenger.storage.scheme.FileLocation;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class AudioMessage extends AbsFileMessage {
    private int duration;

    public AudioMessage(FileLocation location, boolean isDownloaded, int duration, boolean isEncrypted) {
        super(location, isDownloaded, isEncrypted);
        this.duration = duration;
    }

    public AudioMessage(String uploadPath, int duration, boolean isEncrypted) {
        super(uploadPath, isEncrypted);
        this.duration = duration;
    }

    public AudioMessage() {

    }

    @Override
    public AbsFileMessage downloaded() {
        return new AudioMessage(location, true, duration, isEncrypted);
    }

    @Override
    public AbsFileMessage undownloaded() {
        return new AudioMessage(location, false, duration, isEncrypted);
    }

    public AudioMessage uploaded(FileLocation fileLocation) {
        return new AudioMessage(fileLocation, true, duration, isEncrypted);
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        duration = values.getInt(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeInt(4, duration);
    }
}
