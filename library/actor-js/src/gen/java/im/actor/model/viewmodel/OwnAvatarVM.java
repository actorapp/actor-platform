package im.actor.model.viewmodel;

import im.actor.model.mvvm.ValueModel;

/**
 * Created by ex3ndr on 04.03.15.
 */
public class OwnAvatarVM {
    private ValueModel<AvatarUploadState> uploadState = new ValueModel<AvatarUploadState>(
            "avatar.my", new AvatarUploadState(null, false));

    public ValueModel<AvatarUploadState> getUploadState() {
        return uploadState;
    }
}