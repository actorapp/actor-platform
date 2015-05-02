/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.http;

public interface FileDownloadCallback {
    void onDownloaded(byte[] data);

    void onDownloadFailure();
}
