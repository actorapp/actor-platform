package im.actor.model.viewmodel;

/**
 * Upload file callback. Methods always called on background thread.
 */
public interface UploadFileCallback {
    /**
     * On File not uploading
     */
    public void onNotUploading();

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
