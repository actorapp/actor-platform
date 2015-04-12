package im.actor.model.viewmodel;

import im.actor.model.files.FileSystemReference;

/**
 * File State View Model callback. All methods are called in Main Thread.
 */
public interface FileVMCallback {
    /**
     * On file not downloaded
     */
    public void onNotDownloaded();

    /**
     * On file started download
     *
     * @param progress progress in [0..1]
     */
    public void onDownloading(float progress);

    /**
     * On file downloaded
     *
     * @param reference FileSystemReference of downloaded file
     */
    public void onDownloaded(FileSystemReference reference);
}
