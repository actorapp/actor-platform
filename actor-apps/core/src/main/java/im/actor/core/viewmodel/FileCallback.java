/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.files.FileSystemReference;

/**
 * File download callback. All methods are called in background thread.
 */
public interface FileCallback {
    /**
     * On File not downloaded
     */
    @ObjectiveCName("onNotDownloaded")
    void onNotDownloaded();

    /**
     * On download progress
     *
     * @param progress progress in [0..1]
     */
    @ObjectiveCName("onDownloading:")
    void onDownloading(float progress);

    /**
     * On file downloaded
     *
     * @param reference downloaded FileSystemReference
     */
    @ObjectiveCName("onDownloaded:")
    void onDownloaded(FileSystemReference reference);
}