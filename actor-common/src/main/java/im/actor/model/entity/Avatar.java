package im.actor.model.entity;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class Avatar {
    private final AvatarImage smallImage;
    private final AvatarImage largeImage;
    private final AvatarImage fullImage;

    public Avatar(AvatarImage smallImage, AvatarImage largeImage, AvatarImage fullImage) {
        this.smallImage = smallImage;
        this.largeImage = largeImage;
        this.fullImage = fullImage;
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

        if (fullImage != null ? !fullImage.equals(avatar.fullImage) : avatar.fullImage != null) return false;
        if (largeImage != null ? !largeImage.equals(avatar.largeImage) : avatar.largeImage != null) return false;
        if (smallImage != null ? !smallImage.equals(avatar.smallImage) : avatar.smallImage != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = smallImage != null ? smallImage.hashCode() : 0;
        result = 31 * result + (largeImage != null ? largeImage.hashCode() : 0);
        result = 31 * result + (fullImage != null ? fullImage.hashCode() : 0);
        return result;
    }
}