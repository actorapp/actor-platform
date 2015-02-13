package im.actor.messenger.core.images;

import com.droidkit.images.loading.AbsTask;

import im.actor.messenger.storage.scheme.avatar.Avatar;
import im.actor.messenger.storage.scheme.avatar.AvatarImage;

/**
 * Created by ex3ndr on 29.10.14.
 */
public class FullAvatarTask extends AbsTask {

    private AvatarImage avatarImage;

    public FullAvatarTask(Avatar avatar) {
        if (avatar.getFullImage() != null) {
            avatarImage = avatar.getFullImage();
        } else if (avatar.getLargeImage() != null) {
            avatarImage = avatar.getLargeImage();
        } else {
            avatarImage = avatar.getSmallImage();
        }
    }

    public AvatarImage getAvatarImage() {
        return avatarImage;
    }

    @Override
    public String getKey() {
        return "full_avatar:@file=" + avatarImage.getFileLocation().getFileId();
    }
}
