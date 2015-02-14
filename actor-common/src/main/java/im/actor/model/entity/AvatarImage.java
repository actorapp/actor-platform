package im.actor.model.entity;


import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class AvatarImage extends BserObject {

    public static AvatarImage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new AvatarImage(), data);
    }

    private int width;
    private int height;
    private FileLocation fileLocation;

    public AvatarImage(int width, int height, FileLocation fileLocation) {
        this.width = width;
        this.height = height;
        this.fileLocation = fileLocation;
    }

    private AvatarImage() {

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public FileLocation getFileLocation() {
        return fileLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AvatarImage that = (AvatarImage) o;

        if (height != that.height) return false;
        if (width != that.width) return false;
        if (!fileLocation.equals(that.fileLocation)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + fileLocation.hashCode();
        return result;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        width = values.getInt(1);
        height = values.getInt(2);
        fileLocation = FileLocation.fromBytes(values.getBytes(3));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, width);
        writer.writeInt(2, height);
        writer.writeObject(3, fileLocation);
    }
}
