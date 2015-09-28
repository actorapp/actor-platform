/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import im.actor.runtime.mvvm.ValueModel;

/**
 * Current user Avatar View Model
 */
public class OwnAvatarVM {
    @Property("nonatomic, readonly")
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