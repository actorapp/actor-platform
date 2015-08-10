/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.files.FileSystemReference;

/**
 * File State View Model callback. All methods are called in Main Thread.
 */
public interface FileVMCallback {
    /**
     * On file not downloaded
     */
    @ObjectiveCName("onNotDownloaded")
    void onNotDownloaded();

    /**
     * On file started download
     *
     * @param progress progress in [0..1]
     */
    @ObjectiveCName("onDownloading:")
    void onDownloading(float progress);

    /**
     * On file downloaded
     *
     * @param reference FileSystemReference of downloaded file
     */
    @ObjectiveCName("onDownloaded:")
    void onDownloaded(FileSystemReference reference);
}
