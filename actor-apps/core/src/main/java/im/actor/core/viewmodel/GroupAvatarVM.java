/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import im.actor.runtime.mvvm.ValueModel;

/**
 * Group Avatar View Model
 */
public class GroupAvatarVM {
    @Property("nonatomic, readonly")
    private ValueModel<AvatarUploadState> uploadState;

    /**
     * <p>INTERNAL API</p>
     * Create Group Avatar View Model
     *
     * @param gid group's id
     */
    public GroupAvatarVM(int gid) {
        uploadState = new ValueModel<AvatarUploadState>(
                "avatar.group." + gid, new AvatarUploadState(null, false));
    }

    /**
     * Get Upload state Value Model
     *
     * @return Upload state Value Model
     */
    public ValueModel<AvatarUploadState> getUploadState() {
        return uploadState;
    }
}
