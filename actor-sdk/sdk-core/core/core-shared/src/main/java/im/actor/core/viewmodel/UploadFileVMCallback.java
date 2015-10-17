/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * Upload file View Model callback. Methods always called on Main thread.
 */
public interface UploadFileVMCallback {
    /**
     * On File not uploading
     */
    @ObjectiveCName("onNotUploaded")
    void onNotUploaded();

    /**
     * On File upload in progress
     *
     * @param progress progress value in [0..1]
     */
    @ObjectiveCName("onUploading:")
    void onUploading(float progress);

    /**
     * On file uploaded
     */
    @ObjectiveCName("onUploaded")
    void onUploaded();
}
