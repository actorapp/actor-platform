package im.actor.model.viewmodel;

import im.actor.model.files.FileSystemReference;

/**
 * File download callback. All methods are called in background thread.
 */
public interface FileCallback {
    /**
     * On File not downloaded
     */
    public void onNotDownloaded();

    /**
     * On download progress
     *
     * @param progress progress in [0..1]
     */
    public void onDownloading(float progress);

    /**
     * On file downloaded
     *
     * @param reference downloaded FileSystemReference
     */
    public void onDownloaded(FileSystemReference reference);
}