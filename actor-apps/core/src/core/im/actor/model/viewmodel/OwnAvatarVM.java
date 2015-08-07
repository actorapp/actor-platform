/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.viewmodel;

import im.actor.model.mvvm.ValueModel;

/**
 * Current user Avatar View Model
 */
public class OwnAvatarVM {
    private ValueModel<AvatarUploadState> uploadState = new ValueModel<AvatarUploadState>(
            "avatar.my", new AvatarUploadState(null, false));

    /**
     * Get Upload State Value Model
     *
     * @return ValueModel of AvatarUploadState
     */
    public ValueModel<AvatarUploadState> getUploadState() {
        return uploadState;
    }
}