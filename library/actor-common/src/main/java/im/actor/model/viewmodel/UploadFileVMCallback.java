package im.actor.model.viewmodel;

/**
 * Upload file View Model callback. Methods always called on Main thread.
 */
public interface UploadFileVMCallback {
    /**
     * On File not uploading
     */
    public void onNotUploaded();

    /**
     * On File upload in progress
     *
     * @param progress progress value in [0..1]
     */
    public void onUploading(float progress);

    /**
     * On file uploaded
     */
    public void onUploaded();
}
