/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.http;

import com.google.j2objc.annotations.ObjectiveCName;

public interface FileDownloadCallback {
    void onDownloaded(byte[] data);

    @ObjectiveCName("onDownloadFailureWithError:withRetryIn:")
    void onDownloadFailure(int error, int retryInSecs);
}
