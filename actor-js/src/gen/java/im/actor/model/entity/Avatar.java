package im.actor.model.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class Avatar extends BserObject {

    public static Avatar fromBytes(byte[] data) throws IOException {
        return Bser.parse(new Avatar(), data);
    }

    private AvatarImage smallImage;
    private AvatarImage largeImage;
    private AvatarImage fullImage;

    public Avatar(AvatarImage smallImage, AvatarImage largeImage, AvatarImage fullImage) {
        this.smallImage = smallImage;
        this.largeImage = largeImage;
        this.fullImage = fullImage;
    }

    private Avatar() {

    }

    public AvatarImage getSmallImage() {
        return smallImage;
    }

    public AvatarImage getLargeImage() {
        return largeImage;
    }

    public AvatarImage getFullImage() {
        return fullImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Avatar avatar = (Avatar) o;

        if (fullImage != null ? !fullImage.equals(avatar.fullImage) : avatar.fullImage != null)
            return false;
        if (largeImage != null ? !largeImage.equals(avatar.largeImage) : avatar.largeImage != null)
            return false;
        if (smallImage != null ? !smallImage.equals(avatar.smallImage) : avatar.smallImage != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = smallImage != null ? smallImage.hashCode() : 0;
        result = 31 * result + (largeImage != null ? largeImage.hashCode() : 0);
        result = 31 * result + (fullImage != null ? fullImage.hashCode() : 0);
        return result;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        byte[] small = values.optBytes(1);
        if (small != null) {
            smallImage = AvatarImage.fromBytes(small);
        }

        byte[] large = values.optBytes(2);
        if (large != null) {
            largeImage = AvatarImage.fromBytes(large);
        }

        byte[] full = values.optBytes(3);
        if (full != null) {
            fullImage = AvatarImage.fromBytes(full);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (smallImage != null) {
            writer.writeObject(1, smallImage);
        }
        if (largeImage != null) {
            writer.writeObject(2, smallImage);
        }
        if (fullImage != null) {
            writer.writeObject(3, fullImage);
        }
    }
}