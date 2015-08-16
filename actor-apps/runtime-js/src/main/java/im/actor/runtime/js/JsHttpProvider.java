/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;

import im.actor.runtime.HttpRuntime;
import im.actor.runtime.http.FileDownloadCallback;
import im.actor.runtime.http.FileUploadCallback;
import im.actor.runtime.js.http.JsHttpRequest;
import im.actor.runtime.js.http.JsHttpRequestHandler;

public class JsHttpProvider implements HttpRuntime {

    @Override
    public void getMethod(String url, int startOffset, int size, int totalSize, FileDownloadCallback callback) {
        // TODO: Implement
    }

    @Override
    public void putMethod(String url, byte[] contents, final FileUploadCallback callback) {
        JsHttpRequest request = JsHttpRequest.create();
        request.open("PUT", url);
        request.setRequestHeader("Content-Type", "application/octet-stream");
        request.setOnLoadHandler(new JsHttpRequestHandler() {
            @Override
            public void onStateChanged(JsHttpRequest request) {
                if (request.getReadyState() == 4) {
                    if (request.getStatus() == 200) {

                        callback.onUploaded();
                    } else {
                        callback.onUploadFailure();
                    }
                }
            }
        });
        Uint8Array push = TypedArrays.createUint8Array(contents.length);
        for (int i = 0; i < contents.length; i++) {
            push.set(i, contents[i]);
        }
        request.send(push.buffer());
    }
}
