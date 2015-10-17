/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * Upload file callback. Methods always called on background thread.
 */
public interface UploadFileCallback {
    /**
     * On File not uploading
     */
    @ObjectiveCName("onNotUploading")
    void onNotUploading();

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
