package im.actor.messenger.storage.scheme.avatar;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class Avatar extends BserObject {

    private static final int FIELD_SMALL = 1;
    private static final int FIELD_LARGE = 2;
    private static final int FIELD_FULL = 3;

    private AvatarImage smallImage;
    private AvatarImage largeImage;
    private AvatarImage fullImage;

    public Avatar(AvatarImage smallImage, AvatarImage largeImage, AvatarImage fullImage) {
        this.smallImage = smallImage;
        this.largeImage = largeImage;
        this.fullImage = fullImage;
    }

    public Avatar() {
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
    public void parse(BserValues values) throws IOException {
        smallImage = values.optObj(FIELD_SMALL, AvatarImage.class);
        largeImage = values.optObj(FIELD_LARGE, AvatarImage.class);
        fullImage = values.optObj(FIELD_FULL, AvatarImage.class);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (smallImage != null) {
            writer.writeObject(FIELD_SMALL, smallImage);
        }
        if (largeImage != null) {
            writer.writeObject(FIELD_LARGE, largeImage);
        }
        if (fullImage != null) {
            writer.writeObject(FIELD_FULL, fullImage);
        }
    }


}