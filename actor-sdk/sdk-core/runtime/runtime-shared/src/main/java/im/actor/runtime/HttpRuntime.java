/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.http.HTTPResponse;
import im.actor.runtime.promise.Promise;

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
     */
    @ObjectiveCName("getMethodWithUrl:withStartOffset:withSize:withTotalSize:")
    Promise<HTTPResponse> getMethod(String url, int startOffset, int size, int totalSize);

    /**
     * Put File Part. Always need to send only one header: Content-Type: application/octet-stream.
     *
     * @param url      url for upload
     * @param contents content for upload
     */
    @ObjectiveCName("putMethodWithUrl:withContents:")
    Promise<HTTPResponse> putMethod(String url, byte[] contents);
}
