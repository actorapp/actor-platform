/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.http;

import com.google.j2objc.annotations.ObjectiveCName;

public interface FileUploadCallback {
    void onUploaded();

    @ObjectiveCName("onUploadFailureWithError:withRetryIn:")
    void onUploadFailure(int error, int retryInSecs);
}
