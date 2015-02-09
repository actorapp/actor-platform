package im.actor.model.entity;


/**
 * Created by ex3ndr on 09.02.15.
 */
public class AvatarImage {
    private final int width;
    private final int height;
    private final FileLocation fileLocation;

    public AvatarImage(int width, int height, FileLocation fileLocation) {
        this.width = width;
        this.height = height;
        this.fileLocation = fileLocation;
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
}
