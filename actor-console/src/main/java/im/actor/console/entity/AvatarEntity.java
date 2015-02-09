package im.actor.console.entity;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import im.actor.model.entity.Avatar;

import java.io.IOException;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class AvatarEntity extends BserObject {
    private AvatarImageEntity smallImage;
    private AvatarImageEntity largeImage;
    private AvatarImageEntity fullImage;

    public AvatarEntity(Avatar avatar) {
        if (avatar.getSmallImage() != null) {
            smallImage = new AvatarImageEntity(avatar.getSmallImage());
        }
        if (avatar.getLargeImage() != null) {
            largeImage = new AvatarImageEntity(avatar.getLargeImage());
        }
        if (avatar.getFullImage() != null) {
            fullImage = new AvatarImageEntity(avatar.getFullImage());
        }
    }

    public AvatarEntity() {

    }

    public Avatar getAvatar() {
        return new Avatar(
                smallImage != null ? smallImage.getImage() : null,
                largeImage != null ? largeImage.getImage() : null,
                fullImage != null ? fullImage.getImage() : null);
    }

    public AvatarImageEntity getSmallImage() {
        return smallImage;
    }

    public AvatarImageEntity getLargeImage() {
        return largeImage;
    }

    public AvatarImageEntity getFullImage() {
        return fullImage;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        smallImage = values.optObj(1, AvatarImageEntity.class);
        largeImage = values.optObj(2, AvatarImageEntity.class);
        fullImage = values.optObj(3, AvatarImageEntity.class);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (smallImage != null) {
            writer.writeObject(1, smallImage);
        }
        if (largeImage != null) {
            writer.writeObject(2, largeImage);
        }
        if (fullImage != null) {
            writer.writeObject(3, fullImage);
        }
    }
}
