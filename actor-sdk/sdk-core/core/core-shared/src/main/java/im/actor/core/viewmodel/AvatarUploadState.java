/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

/**
 * Avatar upload state
 */
public class AvatarUploadState {
    @Property("nonatomic, readonly")
    private String descriptor;
    @Property("nonatomic, readonly")
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
