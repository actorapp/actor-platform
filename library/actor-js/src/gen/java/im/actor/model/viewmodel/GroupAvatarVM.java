package im.actor.model.viewmodel;

import im.actor.model.mvvm.ValueModel;

/**
 * Created by ex3ndr on 26.03.15.
 */
public class GroupAvatarVM {
    private ValueModel<AvatarUploadState> uploadState;

    public GroupAvatarVM(int gid) {
        uploadState = new ValueModel<AvatarUploadState>(
                "avatar.group." + gid, new AvatarUploadState(null, false));
    }

    public ValueModel<AvatarUploadState> getUploadState() {
        return uploadState;
    }
}
