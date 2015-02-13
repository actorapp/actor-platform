package im.actor.messenger.storage.scheme.avatar;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

import im.actor.messenger.storage.scheme.FileLocation;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class AvatarImage extends BserObject {
    private static final int FIELD_LOCATION = 1;
    private static final int FIELD_WIDTH = 2;
    private static final int FIELD_HEIGHT = 3;

    private FileLocation fileLocation;
    private int width;
    private int height;

    public AvatarImage(FileLocation fileLocation, int width, int height) {
        this.fileLocation = fileLocation;
        this.width = width;
        this.height = height;
    }

    public AvatarImage() {
    }

    public FileLocation getFileLocation() {
        return fileLocation;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void parse(BserValues reader) throws IOException {
        fileLocation = reader.getObj(FIELD_LOCATION, FileLocation.class);
        width = reader.getInt(FIELD_WIDTH);
        height = reader.getInt(FIELD_HEIGHT);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeObject(FIELD_LOCATION, fileLocation);
        writer.writeInt(FIELD_WIDTH, width);
        writer.writeInt(FIELD_HEIGHT, height);
    }
}
