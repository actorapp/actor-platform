/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.http.FileDownloadCallback;
import im.actor.runtime.http.FileUploadCallback;

/**
 * HTTP Requests provider
 */
public interface HttpRuntime {
    /**
     * Get File Part
     *
     * @param url         url for downloading
     * @param startOffset start offset
     * @param size        size of part
     * @param totalSize   total file part
     * @param callback    callback for response
     */
    @ObjectiveCName("getMethodWithUrl:withStartOffset:withSize:withTotalSize:withCallback:")
    void getMethod(String url, int startOffset, int size, int totalSize, FileDownloadCallback callback);

    /**
     * Put File Part. Always need to send only one header: Content-Type: application/octet-stream.
     *
     * @param url      url for upload
     * @param contents content for upload
     * @param callback callback for response
     */
    @ObjectiveCName("putMethodWithUrl:withContents:withCallback:")
    void putMethod(String url, byte[] contents, FileUploadCallback callback);
}
