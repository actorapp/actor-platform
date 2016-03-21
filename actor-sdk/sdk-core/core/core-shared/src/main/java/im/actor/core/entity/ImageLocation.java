package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

import im.actor.core.api.ApiImageLocation;

public class ImageLocation {

    @Property("readonly, nonatomic")
    private int width;
    @Property("readonly, nonatomic")
    private int height;
    @NotNull
    @Property("readonly, nonatomic")
    private FileReference reference;

    public ImageLocation(@NotNull ApiImageLocation imageLocation, @NotNull String fileName) {
        width = imageLocation.getWidth();
        height = imageLocation.getHeight();
        reference = new FileReference(
                imageLocation.getFileLocation(),
                fileName,
                imageLocation.getFileSize());
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @NotNull
    public FileReference getReference() {
        return reference;
    }
}
