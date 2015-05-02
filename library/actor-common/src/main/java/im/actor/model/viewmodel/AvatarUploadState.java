/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.viewmodel;

/**
 * Avatar upload state
 */
public class AvatarUploadState {
    private String descriptor;
    private boolean isUploading;

    public AvatarUploadState(String descriptor, boolean isUploading) {
        this.descriptor = descriptor;
        this.isUploading = isUploading;
    }

    /**
     * Uploading descriptor
     *
     * @return file system descriptor
     */
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * Is upload in progress
     *
     * @return is uploading
     */
    public boolean isUploading() {
        return isUploading;
    }
}
