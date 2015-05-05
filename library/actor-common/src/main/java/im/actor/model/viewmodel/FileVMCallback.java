/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.viewmodel;

import im.actor.model.files.FileSystemReference;

/**
 * File State View Model callback. All methods are called in Main Thread.
 */
public interface FileVMCallback {
    /**
     * On file not downloaded
     */
    void onNotDownloaded();

    /**
     * On file started download
     *
     * @param progress progress in [0..1]
     */
    void onDownloading(float progress);

    /**
     * On file downloaded
     *
     * @param reference FileSystemReference of downloaded file
     */
    void onDownloaded(FileSystemReference reference);
}
