/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.viewmodel;

/**
 * Upload file callback. Methods always called on background thread.
 */
public interface UploadFileCallback {
    /**
     * On File not uploading
     */
    void onNotUploading();

    /**
     * On File upload in progress
     *
     * @param progress progress value in [0..1]
     */
    void onUploading(float progress);

    /**
     * On file uploaded
     */
    void onUploaded();
}
