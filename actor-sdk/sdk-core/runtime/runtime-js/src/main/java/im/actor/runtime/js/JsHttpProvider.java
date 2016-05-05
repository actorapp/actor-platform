/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;

import im.actor.runtime.HttpRuntime;
import im.actor.runtime.http.HTTPError;
import im.actor.runtime.http.HTTPResponse;
import im.actor.runtime.js.http.JsHttpRequest;
import im.actor.runtime.promise.Promise;

public class JsHttpProvider implements HttpRuntime {

    @Override
    public Promise<HTTPResponse> getMethod(String url, int startOffset, int size, int totalSize) {
        return Promise.failure(new RuntimeException("Not implemented!"));
    }

    @Override
    public Promise<HTTPResponse> putMethod(String url, byte[] contents) {
        return new Promise<>(resolver -> {
            JsHttpRequest request = JsHttpRequest.create();
            request.open("PUT", url);
            request.setRequestHeader("Content-Type", "application/octet-stream");
            request.setOnLoadHandler(request1 -> {
                if (request1.getReadyState() == 4) {
                    if (request1.getStatus() >= 200 && request1.getStatus() < 300) {
                        resolver.result(new HTTPResponse(request1.getStatus(), null));
                    } else {
                        resolver.error(new HTTPError(request1.getStatus()));
                    }
                }
            });
            Uint8Array push = TypedArrays.createUint8Array(contents.length);
            for (int i = 0; i < contents.length; i++) {
                push.set(i, contents[i]);
            }
            request.send(push.buffer());
        });
    }
}
