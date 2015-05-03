/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;

import im.actor.model.HttpDownloaderProvider;
import im.actor.model.http.FileDownloadCallback;
import im.actor.model.http.FileUploadCallback;
import im.actor.model.js.providers.http.JsXmlHttpRequest;
import im.actor.model.js.providers.http.JsXmlHttpRequestHandler;

public class JsHttpProvider implements HttpDownloaderProvider {

    @Override
    public void downloadPart(String url, int startOffset, int size, int totalSize, FileDownloadCallback callback) {

    }

    @Override
    public void uploadPart(final String url, byte[] contents, final FileUploadCallback callback) {
        JsXmlHttpRequest request = JsXmlHttpRequest.create();
        request.open("PUT", url);
        request.setRequestHeader("Content-Type", "application/octet-stream");
        request.setOnLoadHandler(new JsXmlHttpRequestHandler() {
            @Override
            public void onStateChanged(JsXmlHttpRequest request) {
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
