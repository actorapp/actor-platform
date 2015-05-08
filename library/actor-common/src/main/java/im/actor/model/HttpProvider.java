/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import im.actor.model.http.FileDownloadCallback;
import im.actor.model.http.FileUploadCallback;

/**
 * HTTP Requests provider
 */
public interface HttpProvider {
    /**
     * Get File Part
     *
     * @param url         url for downloading
     * @param startOffset start offset
     * @param size        size of part
     * @param totalSize   total file part
     * @param callback    callback for response
     */
    void getMethod(String url, int startOffset, int size, int totalSize, FileDownloadCallback callback);

    /**
     * Put File Part. Always need to send only one header: Content-Type: application/octet-stream.
     *
     * @param url      url for upload
     * @param contents content for upload
     * @param callback callback for response
     */
    void putMethod(String url, byte[] contents, FileUploadCallback callback);
}
