package im.actor.messenger.app.images;

import im.actor.images.loading.AbsTask;
import im.actor.model.entity.Avatar;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class AvatarTask extends AbsTask {
    private int size;
    private Avatar avatar;

    public AvatarTask(int size, Avatar avatar) {
        this.size = size;
        this.avatar = avatar;
    }

    public int getSize() {
        return size;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    @Override
    public String getKey() {
        return "avatar:@file=" + avatar.getSmallImage().getFileReference().getFileId() + "@size=" + size;
    }
}
